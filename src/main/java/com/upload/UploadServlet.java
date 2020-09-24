package com.upload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.*;

@WebServlet("/UploadServlet")
public class UploadServlet extends HttpServlet {
    String ROLL_NO_PATTERN = "^20CCTT[0-9][0-9][0-9]$";
    String FULL_DATE_PATTERN = "^[0-9]{1,2}(|\\/)[0-9]{1,2}(|\\/)[0-9]{4}";
    String YEAR_PATTERN = "^[0-9]{4}";
    ArrayList<Student> studentList= new ArrayList<Student>();
    public UploadServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        uploadMultipleFile(request, response);
    }

    protected void getStudentName(String currentLine) {
        boolean isNameToken = false;
        StringTokenizer st = new StringTokenizer(currentLine);
        Student currentStudent = new Student();
        StringBuilder fullName = new StringBuilder();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
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

            if (Pattern.matches(FULL_DATE_PATTERN, token) || Pattern.matches(YEAR_PATTERN, token)) {
                currentStudent.setFullName(fullName.toString());
                this.studentList.add(currentStudent);
                return;
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

    protected void uploadMultipleFile(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
//        request.getRequestDispatcher("/result.jsp").forward(request, response);
    }

}
