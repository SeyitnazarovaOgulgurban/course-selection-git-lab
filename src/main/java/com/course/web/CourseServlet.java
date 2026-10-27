package com.course.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 课程数据接口：GET /api/courses
 * 不连接数据库；演示课程均定义在后端 Java 代码中。
 */
@WebServlet(name = "CourseServlet", urlPatterns = "/api/courses")
public class CourseServlet extends HttpServlet {

    // 字段：课程ID、名称、教师、分类、星期、节次起止、学分、教室、描述
    static final List<Course> COURSES = List.of(
        new Course("CS101", "Java程序设计", "王老师", "专业核心", 1, 1, 2, 3.0, "A201", "面向对象基础、集合与实践项目"),
        new Course("CS102", "Web前端开发", "李老师", "专业核心", 1, 3, 4, 3.0, "A305", "HTML、CSS 与 JavaScript 开发"),
        new Course("CS103", "数据库原理", "陈老师", "专业核心", 2, 1, 2, 3.0, "B201", "关系模型、SQL 与数据库设计"),
        new Course("CS104", "计算机网络", "张老师", "专业核心", 3, 3, 4, 3.0, "B105", "TCP/IP、网络协议与网络应用"),
        new Course("CS105", "Python数据分析", "黄老师", "专业选修", 2, 1, 2, 2.0, "A212", "使用 Python 处理和分析数据"),
        new Course("CS106", "人工智能导论", "赵老师", "专业选修", 4, 5, 6, 2.0, "A315", "机器学习和人工智能基础概念"),
        new Course("CS107", "信息安全实践", "孙老师", "实践课程", 5, 1, 2, 2.0, "实训302", "安全基础知识与实际操作"),
        new Course("CS108", "学术英语", "周老师", "通识课程", 2, 5, 6, 2.0, "C204", "学术阅读、表达与演讲训练"),
        new Course("CS109", "创新设计思维", "刘老师", "通识课程", 4, 1, 2, 1.5, "C112", "设计思维、协作与创新实践"),
        new Course("CS110", "移动应用开发", "何老师", "实践课程", 5, 3, 4, 2.5, "实训402", "移动端界面与应用程序实现")
    );

    static Course findCourse(String courseId) {
        if (courseId == null) return null;
        for (Course course : COURSES) {
            if (course.id.equals(courseId)) return course;
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        prepareJson(response);
        StringBuilder json = new StringBuilder("{\"success\":true,\"courses\":[");
        for (int i = 0; i < COURSES.size(); i++) {
            if (i > 0) json.append(',');
            json.append(COURSES.get(i).toJson());
        }
        json.append("]}");
        response.getWriter().write(json.toString());
    }

    static void prepareJson(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
    }

    // 使用原生 Java 拼接合法 JSON，无需 Gson / Jackson 等第三方库。
    static String jsonString(String input) {
        if (input == null) return "null";
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                case '\b': out.append("\\b"); break;
                case '\f': out.append("\\f"); break;
                default:
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
            }
        }
        return out.append('"').toString();
    }

    static final class Course {
        final String id, name, teacher, category, room, description;
        final int weekday, startPeriod, endPeriod;
        final double credits;

        Course(String id, String name, String teacher, String category,
               int weekday, int startPeriod, int endPeriod, double credits,
               String room, String description) {
            this.id = id;
            this.name = name;
            this.teacher = teacher;
            this.category = category;
            this.weekday = weekday;
            this.startPeriod = startPeriod;
            this.endPeriod = endPeriod;
            this.credits = credits;
            this.room = room;
            this.description = description;
        }

        boolean conflictsWith(Course other) {
            return weekday == other.weekday
                && startPeriod <= other.endPeriod
                && other.startPeriod <= endPeriod;
        }

        String toJson() {
            return "{\"id\":" + jsonString(id)
                + ",\"name\":" + jsonString(name)
                + ",\"teacher\":" + jsonString(teacher)
                + ",\"category\":" + jsonString(category)
                + ",\"weekday\":" + weekday
                + ",\"startPeriod\":" + startPeriod
                + ",\"endPeriod\":" + endPeriod
                + ",\"credits\":" + credits
                + ",\"room\":" + jsonString(room)
                + ",\"description\":" + jsonString(description)
                + "}";
        }
    }
}
