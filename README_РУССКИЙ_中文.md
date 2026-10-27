# CourseFlow — 选课系统（Tomcat 10.1 / jakarta.servlet）

## Что внутри

Это готовый учебный проект Java Web: **ровно 2 Java Servlet + 2 статические HTML-страницы**. Нет базы данных, Spring, Bootstrap, jQuery, Gson или других фреймворков. JavaScript и CSS встроены в два HTML-файла.

Четыре исходных файла, которые нужно сдавать преподавателю:

1. `src/main/java/com/course/web/CourseServlet.java` — GET `/api/courses`, десять курсов прописаны в Java-коде.
2. `src/main/java/com/course/web/EnrollmentServlet.java` — GET и POST `/api/enrollments`, хранит записи через `HttpSession`, проверяет повторы, конфликты расписания и максимум пять курсов.
3. `src/main/webapp/index.html` — каталог курсов, поиск, фильтрация, кнопка выбора, статистика.
4. `src/main/webapp/my-courses.html` — выбранные курсы, недельное расписание, удаление курсов.

Файл `pom.xml` нужен **только для сборки**, не входит в четыре исходных файла для сдачи.

## Требования

- JDK 11 или новее (рекомендуется JDK 17 или 21).
- Apache Tomcat **10.1.x** (также подходит Tomcat 11 с JDK 17+). НЕ использовать эту версию проекта с Tomcat 9 — там нужен пакет `javax.servlet`.
- Maven 3.8+ (или Maven внутри IntelliJ IDEA).

## Как запустить в IntelliJ IDEA — самый удобный способ

1. Распаковать архив **полного проекта**.
2. В IntelliJ IDEA нажать `File → Open`, выбрать папку `CourseSelection_Tomcat10` (в ней находится `pom.xml`).
3. Подождать, пока Maven загрузит библиотеки. Установить Project SDK JDK 17 или 21.
4. В терминале IntelliJ IDEA выполнить:

   ```bash
   mvn clean package
   ```

5. Появится `target/course-selection.war`. Скопировать его в папку `webapps` установленного Tomcat 10.1.
6. Запустить Tomcat через `bin/startup.bat` (Windows) или `bin/startup.sh` (macOS/Linux).
7. В браузере открыть: **http://localhost:8080/course-selection/**.

Если IntelliJ IDEA Ultimate поддерживает конфигурацию Tomcat, можно создать `Run → Edit Configurations → + → Tomcat Server (Local)` и добавить Artifact `course-selection:war exploded`, затем запустить. URL зависит от установленного Application context (путь `/course-selection` рекомендуется).

**Не открывайте `index.html` двойным щелчком** через `file://`: браузеру нужны запущенные Servlet API.

Если порт 8080 занят, используйте фактический HTTP-порт вашего Tomcat.

## Демонстрация DevTools (важно для преподавателя)

В браузере откройте `F12 → Network → Fetch/XHR` (или `All`). Нажмите `Ctrl+R` для обновления страницы.

| Действие | HTTP | URL | Где смотреть |
|---|---|---|---|
| Открыть каталог | GET | `/course-selection/api/courses` | `Response`: JSON с 10 курсами |
| Открыть «我的课程» | GET | `/course-selection/api/enrollments` | `Response`: selectedIds, count, totalCredits |
| Нажать «立即选课» | POST | `/course-selection/api/enrollments` | `Payload`: `action=add&courseId=CS101` |
| Нажать «退选课程» | POST | `/course-selection/api/enrollments` | `Payload`: `action=remove&courseId=CS101` |

При успешной операции статус `200`. При конфликте времени или повторной записи сервер отвечает `409` с JSON-сообщением об ошибке. При некорректном ID — `400`.

**Как показать конфликт:** сначала добавить `CS103 数据库原理` (вторник, 1–2), затем попытаться добавить `CS105 Python数据分析` (тоже вторник, 1–2). Сервер не даст добавить второй курс.

## Схема обмена данными

```text
index.html ───── GET /api/courses ───────────────→ CourseServlet.java
     │                                                │
     │ ←────── JSON (список курсов) ───────────────────┘
     │
     └────────── POST /api/enrollments ────────────→ EnrollmentServlet.java
my-courses.html ─ GET /api/enrollments ──────────→   │
my-courses.html ─ POST /api/enrollments ─────────→   ├─ HttpSession
                       ↑                           └─ hard-coded CourseServlet.COURSES
                       └──────── JSON ────────────────┘
```

Данные хранятся **только на время HTTP-сессии** и сбрасываются при её истечении или перезапуске сервера. Это сознательно сделано под условие домашнего задания: без базы данных.

## 中文：课堂展示讲解（约 1 分钟）

老师好！我的项目是基于 Tomcat 的 Java Web 选课系统，使用两个 Servlet 和两个静态 HTML 页面，没有使用数据库，也没有使用 Spring 等第三方框架。

`CourseServlet` 负责通过 GET 请求返回课程列表，课程信息直接写在 Java 后端代码中。`EnrollmentServlet` 通过 GET 查询当前用户的已选课程，通过 POST 实现选课和退课。为了保存当前用户的数据，我使用了 `HttpSession`。

前端页面通过原生 JavaScript 的 `fetch()` 与后端 Servlet 通信，数据格式为 JSON。系统还支持课程搜索、分类筛选、个人课表、重复选课检查、时间冲突检查和最多选五门课程。

接下来我打开浏览器开发者工具的 Network 面板，展示 GET 请求获取课程信息，以及 POST 请求提交 `action` 和 `courseId` 参数，并查看服务器返回的 JSON 数据。

## 常见问题

- `404 /api/courses`：检查 WAR 是否已部署、Tomcat 是否已启动、网址中的 context path 是否为 `/course-selection`。
- `jakarta.servlet...` 找不到：请确保使用 **Tomcat 10.1** 和这个版本的项目；如果 Tomcat 是 9，使用另一个标记为 `Tomcat9` 的项目。
- `mvn` 不识别：安装 Maven，或在 IntelliJ IDEA 的 Maven 面板执行 `Lifecycle → package`。
- 关闭、重启或更换浏览器后看不到数据：本项目使用 `HttpSession`，不是数据库。
