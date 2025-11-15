
import java.io.*;
import java.net.*;

public class ByteServer {

    public static void main(String[] args) {
        int port = 5000;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Listening on port " + port + "...");
            Socket socket = serverSocket.accept();
            System.out.println("Client connected: " + socket.getInetAddress());

            while (true) {
                InputStream in = socket.getInputStream();

                byte[] buffer = new byte[1024];
                int bytesRead;

                char[] command_buffer = new char[]{'c', 'o', 'l', 'd', 's'};
                StringBuilder arg_buffer = new StringBuilder();
                boolean read_arg_flag = false;

                boolean read_file_bytes_flag = false;
                String file_name_and_path = null;

                /* Valid commands
                 * *MKF*(path+filename)*
                 * *EOF*
                 */
                // Read until client closes connection
                while ((bytesRead = in.read(buffer)) != -1) {
                    //System.out.println("Read " + bytesRead + " bytes:");
                    for (int i = 0; i < bytesRead; i++) {

                        String command = update_buffer(command_buffer, (char) buffer[i]);
                        // Command read from buffer
                        if (command != null) {
                            System.out.println("COMMAND: " + command);

                            // Case make file command
                            if (command.equals("*MKF*")) {
                                read_arg_flag = true;
                            }

                            // Case end of file command
                            if (command.equals("*EOF*")) {
                                read_file_bytes_flag = false;
                            }

                        }

                        // Read the file contents
                        if (read_arg_flag) {
                            file_name_and_path = update_arg_buffer(arg_buffer, (char) buffer[i]);

                            // Finished reading the file name
                            if (file_name_and_path != null) {
                                // Handle dir creation
                                String path = get_path(file_name_and_path);
                                System.out.println(path);
                                create_path(path);
                                read_arg_flag = false;
                                read_file_bytes_flag = true;

                                // Handle file creation
                                create_file(file_name_and_path);
                            }
                        }

                        if (read_file_bytes_flag) {
                            write_byte_to_file(file_name_and_path, buffer[i]);
                        }

                        //System.out.print((char) buffer[i]); // or print raw bytes if you want
                    }
                    //System.out.println();
                }

                //socket.close();
                //System.out.println("Client disconnected.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean write_byte_to_file(String file_path, byte data) {
        try (FileOutputStream fos = new FileOutputStream(file_path, true)) {
            fos.write(data);
            //System.out.println("Byte"+(char)data+" successfully written to " + file_path);
            return true;
        } catch (Exception e) {
            System.out.println("An error occured writting to " + file_path);
            e.printStackTrace();
            return false;
        }
    }

    public static boolean create_file(String full_path) {
        try {
            File new_file = new File(full_path);
            if (new_file.createNewFile()) {
                System.out.println("New file " + full_path + " created");
                return true;
            } else {
                System.out.println("File " + full_path + " already exists");
                return true;
            }
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return false;
    }

    public static String get_path(String full_path_file) {
        String[] split_path = full_path_file.split("/");

        StringBuilder path = new StringBuilder();
        for (int i = 0; i < split_path.length - 1; i++) {
            path.append(split_path[i]).append('/');
        }

        return new String(path);
    }

    public static boolean create_path(String rel_path) {
        File new_dir = new File(rel_path);
        if (new_dir.exists()) {
            System.out.println("Path " + new_dir.getAbsolutePath() + " already exists");
            return true;
        }
        if (new_dir.mkdirs()) {
            System.out.println("Creating path " + new_dir.getAbsolutePath());
            return true;
        }
        System.out.println("Failed to create new path");
        return false;
    }

    public static String update_arg_buffer(StringBuilder arg_buffer, char c) {
        if (c == '*' && arg_buffer.length() > 0) {
            arg_buffer.delete(0, 1);
            String out = new String(arg_buffer);
            arg_buffer.setLength(0);
            return out;
        }
        arg_buffer.append(c);
        return null;
    }

    public static String update_buffer(char[] command_buffer, char c) {
        /* Update the buffer sliding window that listens for commands */

        // Move everything one over
        for (int i = 0; i < 4; i++) {
            command_buffer[i] = command_buffer[i + 1];
        }

        // Replace last with with the new char
        command_buffer[4] = c;

        // if stars at the start and end return the string
        if (command_buffer[0] == '*' && command_buffer[4] == '*') {
            return new String(command_buffer);
        }
        return null;
    }

}
