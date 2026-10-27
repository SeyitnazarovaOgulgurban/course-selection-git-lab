package com.course.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 我的选课接口：
 *   GET  /api/enrollments -> 查看当前浏览器会话的已选课程
 *   POST /api/enrollments -> action=add/remove&courseId=CS101
 * 选课记录使用 HttpSession 保存；没有数据库，也没有第三方框架。
 */
@WebServlet(name = "EnrollmentServlet", urlPatterns = "/api/enrollments")
public class EnrollmentServlet extends HttpServlet {
    private static final String SESSION_KEY = "selectedCourseIds";
    private static final int COURSE_LIMIT = 5;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CourseServlet.prepareJson(response);
        HttpSession session = request.getSession(true);
        synchronized (session) {
            response.getWriter().write("{\"success\":true,\"data\":" +
                stateJson(selectedIds(session)) + "}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        CourseServlet.prepareJson(response);

        String action = request.getParameter("action");
        String courseId = request.getParameter("courseId");
        if (!"add".equals(action) && !"remove".equals(action)) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "操作无效，请使用 add 或 remove。");
            return;
        }
        CourseServlet.Course target = CourseServlet.findCourse(courseId);
        if (target == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "课程编号不存在。");
            return;
        }

        HttpSession session = request.getSession(true);
        synchronized (session) {
            Set<String> selected = selectedIds(session);
            if ("add".equals(action)) {
                if (selected.contains(courseId)) {
                    sendError(response, HttpServletResponse.SC_CONFLICT, "你已经选择过这门课了。");
                    return;
                }
                if (selected.size() >= COURSE_LIMIT) {
                    sendError(response, HttpServletResponse.SC_CONFLICT,
                        "最多只能选择 " + COURSE_LIMIT + " 门课程。");
                    return;
                }
                for (String existingId : selected) {
                    CourseServlet.Course existing = CourseServlet.findCourse(existingId);
                    if (existing != null && target.conflictsWith(existing)) {
                        sendError(response, HttpServletResponse.SC_CONFLICT,
                            "选课时间冲突：与《" + existing.name + "》上课时间重叠。");
                        return;
                    }
                }
                selected.add(courseId);
            } else {
                if (!selected.remove(courseId)) {
                    sendError(response, HttpServletResponse.SC_CONFLICT, "你还没有选择这门课。");
                    return;
                }
            }
            String message = "add".equals(action) ? "选课成功！" : "退课成功！";
            response.getWriter().write("{\"success\":true,\"message\":"
                + CourseServlet.jsonString(message) + ",\"data\":" + stateJson(selected) + "}");
        }
    }

    @SuppressWarnings("unchecked")
    private static Set<String> selectedIds(HttpSession session) {
        Set<String> existing = (Set<String>) session.getAttribute(SESSION_KEY);
        if (existing == null) {
            existing = new LinkedHashSet<>();
            session.setAttribute(SESSION_KEY, existing);
        }
        return existing;
    }

    private static String stateJson(Set<String> selected) {
        StringBuilder ids = new StringBuilder("[");
        StringBuilder courses = new StringBuilder("[");
        double credits = 0.0;
        int count = 0;
        for (String id : selected) {
            CourseServlet.Course course = CourseServlet.findCourse(id);
            if (course == null) continue;
            if (count++ > 0) {
                ids.append(',');
                courses.append(',');
            }
            ids.append(CourseServlet.jsonString(id));
            courses.append(course.toJson());
            credits += course.credits;
        }
        return "{\"selectedIds\":" + ids.append(']')
            + ",\"courses\":" + courses.append(']')
            + ",\"count\":" + count
            + ",\"totalCredits\":" + credits
            + ",\"limit\":" + COURSE_LIMIT + "}";
    }

    private static void sendError(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.getWriter().write("{\"success\":false,\"message\":"
            + CourseServlet.jsonString(message) + "}");
    }
}
