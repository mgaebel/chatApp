package ChatApp.service;

import com.google.gson.Gson;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
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

import static com.google.common.base.Objects.equal;
import static org.springframework.util.StringUtils.hasText;

@RestController
public class FileServerEndpointController {

    @RequestMapping(value = "/fileTree", method = RequestMethod.POST )
    public List<FileSystemNode> fileTree(@RequestParam( value = "pathRoot", defaultValue = "") String pathRoot, HttpServletResponse httpServletResponse) throws IOException {
        if( !hasText(pathRoot) || equal( pathRoot, "undefined" ) ){
            List<FileSystemNode> rootList = new ArrayList<>();
            rootList.add( new FileSystemNode("Anime", "Anime", "directory", "" ) );
            rootList.add( new FileSystemNode("Movies", "Movies", "directory", "" ) );
            rootList.add( new FileSystemNode("Other", "Other", "directory", "" ) );
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
                readableFileSize( path.toFile().length() )
        ) ) );
    }

    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @RequestMapping( value = "download")
    public void download( @RequestParam("path") String path, HttpServletResponse response ) throws IOException {
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

    @RequestMapping( value = "downloadDir")
    public void downloadDir( @RequestParam("path") String path, HttpServletResponse response ) throws IOException {
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

    private class FileSystemNode {
        private String path;
        private String name;
        private String type;
        private String size;
        private List<FileSystemNode> children;

        public FileSystemNode(){
            children = new ArrayList<>();
        }

        public FileSystemNode(String path, String name, String type, String size){
            this.path = path;
            this.name = name;
            this.type = type;
            this.size = size;
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
    }
}
