package com.upload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.*;


@WebServlet("/UploadServlet")
public class UploadServlet extends HttpServlet {
    private final String ROLL_NO_PATTERN = "^20CCTT[0-9][0-9][0-9]$";
    private final String FULL_DATE_PATTERN = "^[0-9]{1,2}(|\\/)[0-9]{1,2}(|\\/)[0-9]{2,4}";
    private final String YEAR_PATTERN = "^[0-9]{4}";
    protected ArrayList<Student> studentList = new ArrayList<Student>();
    private final String DOWNLOAD_PATH = "/Users/TuanTinhTe/Desktop/Java Web/DownloadStudentName/src/mp3";

    public UploadServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        uploadMultipleFile(request, response);
    }

    protected void downloadNameAudio(Student student) throws IOException {
        String filePath = DOWNLOAD_PATH + File.separator + student.getRollNo() + ".mp3";
        File target = new File(filePath);
        String postUrl = "https://es.vbeecore.com/api/tts";
        JSONObject request = new JSONObject();
        request.put("input_text", student.getFullName());
        request.put("voice", "hn_male_manhdung_news_48k-h");
        request.put("app_id", "d8a535cc0b3508a6647ec5f3");
        request.put("user_id", "2");
        request.put("time", "1594711709658");
        request.put("key", "42a1d25a3b8b0810a6fe98d1fd2e5ae0");
        request.put("audio_type", "mp3");
        request.put("rate", "0.7");

        try {
            URL obj = new URL(postUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(request.toString().getBytes("UTF-8"));
            os.flush();
            os.close();
            InputStream is = con.getInputStream();
            Files.copy(is, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            is.close();
            con.disconnect();
            System.out.println("File" + student.getRollNo() + ".mp3" + " downloaded");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("POST request not worked");
        }
    }

    private static String md5(String text) {
        try {
            MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(text.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    protected void downloadNameMP3(Student student) throws IOException {
        String SOUND_URL = "https://vbee.vn/api/v1/convert-tts-api";
        String POST_PARAMS = "username=0904186221&" +
                "input_text=" + URLEncoder.encode(student.getFullName(), "UTF-8") + "&" +
                "dictionary_id=5cdc3c391f7aae0619218024&" +
                "application_id=76a14a1edbc8c34d255e6e9f&" +
                "voice=sg_male_minhhoang_news_48k-d&" +
                "rate=1&" +
                "audio_type=mp3&" +
                "type_output=link&" +
                "input_type=text&" +
                "bit_rate=128000&" +
                "type_campaign=1&" +
                "url_callback_api=https://vbee.vn/api/v1/callback-tts-demo";

        URL obj = new URL(SOUND_URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("charset", "utf-8");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(POST_PARAMS.getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();

        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedInputStream inputStream = new BufferedInputStream(con.getInputStream());
            String saveFilePath = DOWNLOAD_PATH + File.separator + student.getRollNo() + ".mp3";
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

            int bytesRead = -1;
            byte[] buffer = new byte[4069];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            System.out.println("File" + student.getRollNo() + ".mp3" + " downloaded");
        } else {
            System.out.println("POST request not worked");
        }
    }

    protected void getStudentName(String currentLine) {
        boolean isNameToken = false;
        StringTokenizer st = new StringTokenizer(currentLine);
        Student currentStudent = new Student();
        StringBuilder fullName = new StringBuilder();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            if (Pattern.matches(FULL_DATE_PATTERN, token) || Pattern.matches(YEAR_PATTERN, token)) {
                currentStudent.setFullName(fullName.toString());
                this.studentList.add(currentStudent);
                return;
            }

            if (isNameToken) {
                if (fullName.toString().equals("")) {
                    fullName.append(token);
                } else {
                    fullName.append(" ").append(token);
                }
            }

            if (Pattern.matches(ROLL_NO_PATTERN, token)) {
                currentStudent.setRollNo(token);
                isNameToken = true;
            }

        }
    }

    protected void readTextFile(String filePath) {
        try {
            Scanner myReader = new Scanner(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_16));
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                System.out.println(data);
                getStudentName(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    protected void uploadMultipleFile(HttpServletRequest request, HttpServletResponse response) throws
            ServletException, IOException {
        String UPLOAD_DIRECTORY = "/Users/TuanTinhTe/Desktop/Java Web/DownloadStudentName/src/public";
        if (ServletFileUpload.isMultipartContent(request)) {
            try {
                List<FileItem> multiparts = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
                for (FileItem item : multiparts) {
                    if (!item.isFormField()) {
                        String fileName = new File(item.getName()).getName();
                        String filePath = UPLOAD_DIRECTORY + File.separator + fileName;
                        item.write(new File(filePath));
                        readTextFile(filePath);
                    }
                }
                request.setAttribute("message", "File uploaded successfully.");
            } catch (Exception ex) {
                request.setAttribute("message", "File upload failed due to : " + ex);
            }
        } else {
            request.setAttribute("message", "Sorry this servlet only handles file upload request.");
        }

        for (Student student : this.studentList) {
            downloadNameAudio(student);
        }
        System.out.println("Done download audio!!!");
//        request.getRequestDispatcher("/result.jsp").forward(request, response);
    }

}
