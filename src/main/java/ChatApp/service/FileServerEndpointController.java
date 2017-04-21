package ChatApp.service;

import com.google.gson.Gson;
import com.sun.nio.zipfs.ZipDirectoryStream;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static com.google.common.base.Objects.equal;
import static org.springframework.util.StringUtils.hasText;

@RestController
public class FileServerEndpointController {

    @RequestMapping(value = "/fileTree", method = RequestMethod.POST )
    public List<FileSystemNode> fileTree(@RequestParam( value = "pathRoot", defaultValue = "") String pathRoot, HttpServletResponse httpServletResponse) throws IOException {
        if( !hasText(pathRoot) || equal( pathRoot, "undefined" ) ){
            List<FileSystemNode> rootList = new ArrayList<>();
            rootList.add( new FileSystemNode("Anime", "Anime", "directory", "", true ) );
            rootList.add( new FileSystemNode("Movies", "Movies", "directory", "", true ) );
            rootList.add( new FileSystemNode("Other", "Other", "directory", "", true ) );
            return rootList;
        } else {
            List<FileSystemNode> directoryList = new ArrayList<>();
            try {
                switch (pathRoot){
                    case "Anime" : {
                        populateDirList("Q:\\Anime", directoryList);
                        populateDirList("L:\\Anime 2", directoryList);
                        populateDirList("F:\\AnimeShare", directoryList);
                        directoryList.sort((o1, o2) -> o1.name.compareTo(o2.name));
                        break;
                    }
                    case "Movies" : {
                        populateDirList("E:\\Users\\Media\\Desktop\\Communal\\Movies", directoryList);
                        populateDirList("F:\\MovieShare", directoryList);
                        directoryList.sort((o1, o2) -> o1.name.compareTo(o2.name));
                        break;
                    }
                    case "Other" : {
                        populateDirList("F:\\OtherShare", directoryList);
                        break;
                    }
                    default : {
                        populateDirList(pathRoot, directoryList);
                    }
                }
            } catch (IOException e ){
                Gson gson = new Gson();
                gson.toJson("ERROR"+e.getLocalizedMessage(), httpServletResponse.getWriter());
            }
            return directoryList;
        }
    }

    private void populateDirList(String pathRoot, List<FileSystemNode> directoryList) throws IOException {
        Stream<Path> contents = Files.list(Paths.get(new File(pathRoot).toURI()));
        contents.forEach( path -> directoryList.add( new FileSystemNode(
                path.toFile().getAbsolutePath(),
                path.toFile().getName(),
                path.toFile().isDirectory() ? "directory" : "file",
                readableFileSize( path.toFile().length() ),
                false
        ) ) );
    }

    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @RequestMapping( value = "download")
    public void download( @RequestParam("path") String path, @RequestParam("zip") boolean zip, HttpServletResponse response ) throws IOException {
        if( hasText(path) ){
            File file = new File( path );
            response.setContentType("application/octet-stream");
            response.setContentLength((int) file.length());
            response.setHeader( "Content-Disposition",
                    String.format("attachment; filename=\"%s\"", file.getName()));

            OutputStream out = response.getOutputStream();
            try (FileInputStream in = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
            out.flush();
        }
    }

    @RequestMapping( value = "zipDir")
    public ZipResponse zipDir( @RequestParam("path") String path, HttpServletResponse response ) throws IOException {
        if( hasText(path) ){
            File directory = new File( path );
            String zipPath = "C:\\temp\\"+ directory.getName() +".zip";
            if( Files.exists( Paths.get("C:\\temp\\"+ directory.getName() +".zip") ) ){
                return new ZipResponse( zipPath );
            }
            File zipDir = new File( zipPath );
            zipDirectory(directory, zipDir);
            return new ZipResponse( zipPath );
        }
        return new ZipResponse("");
    }


    public void zipDirectory(File dir, File zipFile) throws IOException {
        FileOutputStream fout = new FileOutputStream(zipFile);
        ZipOutputStream zout = new ZipOutputStream(fout);
        zipSubDirectory("", dir, zout);
        zout.close();
    }

    private void zipSubDirectory(String basePath, File dir, ZipOutputStream zout) throws IOException {
        byte[] buffer = new byte[4096];
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                String path = basePath + file.getName() + "/";
                zout.putNextEntry(new ZipEntry(path));
                zipSubDirectory(path, file, zout);
                zout.closeEntry();
            } else {
                FileInputStream fin = new FileInputStream(file);
                zout.putNextEntry(new ZipEntry(basePath + file.getName()));
                int length;
                while ((length = fin.read(buffer)) > 0) {
                    zout.write(buffer, 0, length);
                }
                zout.closeEntry();
                fin.close();
            }
        }
    }

    private class ZipResponse {
        private String zipPath;

        public ZipResponse() {}

        public ZipResponse(String zipPath){
            this.zipPath = zipPath;
        }

        public String getZipPath() {
            return zipPath;
        }

        public void setZipPath(String zipPath) {
            this.zipPath = zipPath;
        }
    }

    private class FileSystemNode {
        private String path;
        private String name;
        private String type;
        private String size;
        private Boolean root;
        private List<FileSystemNode> children;

        public FileSystemNode(){
            children = new ArrayList<>();
        }

        public FileSystemNode(String path, String name, String type, String size, boolean root){
            this.path = path;
            this.name = name;
            this.type = type;
            this.size = size;
            this.root = root;
            children = new ArrayList<>();
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public List<FileSystemNode> getChildren() {
            return children;
        }

        public void setChildren(List<FileSystemNode> children) {
            this.children = children;
        }

        public Boolean getRoot() {
            return root;
        }

        public void setRoot(Boolean root) {
            this.root = root;
        }
    }
}
