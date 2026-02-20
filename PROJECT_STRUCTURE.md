# Library Manager – Project Structure & MVC Flow

Dự án: Quản lý thư viện  
Công nghệ: Spring Boot – Thymeleaf – Spring Data JPA – SQL Server  
Mô hình kiến trúc: MVC (View – Controller – Model)

---

## 1. Kiến trúc tổng thể (MVC)

```

View (Thymeleaf)
|
v
Controller
|
v
Model
├─ DAO (Repository)
└─ Entities (JPA)
|
v
SQL Server

```

---

## 2. Cấu trúc thư mục dự án

```

src/main/java/vn/edu/hsf/librarymanager
│
├─ controller
│   ├─ BookController.java
│   ├─ BorrowController.java
│   ├─ OrderController.java
│   ├─ StudentController.java
│   ├─ StaffController.java
│   └─ CategoryController.java
│
├─ model
│   ├─ entity
│   │   ├─ Role.java
│   │   ├─ Staff.java
│   │   ├─ Student.java
│   │   ├─ Category.java
│   │   ├─ Publisher.java
│   │   ├─ Author.java
│   │   ├─ Book.java
│   │   ├─ BookAuthor.java
│   │   ├─ BookCode.java
│   │   ├─ Price.java
│   │   ├─ BookPrice.java
│   │   ├─ Borrow.java
│   │   ├─ BorrowItem.java
│   │   ├─ Orders.java
│   │   ├─ OrderDetail.java
│   │   └─ BookFile.java
│   │
│   └─ dao
│       ├─ RoleDAO.java
│       ├─ StaffDAO.java
│       ├─ StudentDAO.java
│       ├─ CategoryDAO.java
│       ├─ PublisherDAO.java
│       ├─ AuthorDAO.java
│       ├─ BookDAO.java
│       ├─ BookAuthorDAO.java
│       ├─ BookCodeDAO.java
│       ├─ PriceDAO.java
│       ├─ BookPriceDAO.java
│       ├─ BorrowDAO.java
│       ├─ BorrowItemDAO.java
│       ├─ OrdersDAO.java
│       ├─ OrderDetailDAO.java
│       └─ BookFileDAO.java
│
└─ LibraryManagerApplication.java

src/main/resources
│
├─ templates
│   ├─ layout
│   │   ├─ header.html
│   │   └─ footer.html
│   │
│   ├─ books
│   │   ├─ list.html
│   │   ├─ form.html
│   │   └─ detail.html
│   │
│   ├─ borrows
│   │   ├─ list.html
│   │   ├─ form.html
│   │   └─ detail.html
│   │
│   ├─ orders
│   │   ├─ list.html
│   │   └─ detail.html
│   │
│   └─ students
│       ├─ list.html
│       └─ form.html
│
├─ static
│   ├─ css
│   └─ js
│
└─ application.properties

```

---

## 3. Model – ánh xạ từ database

Hệ thống entity và quan hệ được xây dựng tương ứng với các bảng trong database:

- Role – Staff – StaffRole (N–N)
- Student
- Category
- Publisher
- Author
- Book
- BookAuthor (N–N)
- BookCode (1–N)
- Price
- BookPrice (giá theo thời gian)
- Borrow
- BorrowItem
- Orders
- OrderDetail
- BookFile

(Các bảng và quan hệ này lấy trực tiếp từ script thiết kế CSDL của dự án)

---

## 4. Luồng xử lý nghiệp vụ theo MVC

### 4.1. Luồng xem danh sách sách

```

Browser
|
| GET /books
v
BookController.list()
|
v
BookDAO.findAll()
|
v
SQL Server
|
v
Controller trả model
|
v
templates/books/list.html

```

---

### 4.2. Luồng thêm mới sách

```

Browser
|
| GET /books/new
v
BookController.showCreateForm()
|
v
templates/books/form.html

Browser
|
| POST /books
v
BookController.create()
|
v
BookDAO.save(Book)
|
v
SQL Server
|
v
redirect:/books

```

---

### 4.3. Luồng lập phiếu mượn sách (Borrow)

```

Browser
|
| GET /borrows/new
v
BorrowController.showCreateForm()
|
| load Student, Book
v
templates/borrows/form.html

```

Khi người dùng bấm lưu:

```

Browser
|
| POST /borrows
v
BorrowController.createBorrow()
|
v
BorrowDAO.save(Borrow)
BorrowItemDAO.save(...)
BookDAO.update(Available)
|
v
SQL Server
|
v
redirect:/borrows

```

---

### 4.4. Luồng trả sách

```

Browser
|
| POST /borrows/{id}/return
v
BorrowController.returnBooks()
|
v
BorrowDAO.update(Status = Returned)
BookDAO.update(Available)
|
v
SQL Server

```

---

### 4.5. Luồng bán sách (Orders)

```

Browser
|
| POST /orders
v
OrderController.createOrder()
|
v
OrdersDAO.save(Orders)
OrderDetailDAO.save(...)
BookDAO.update(Available)
|
v
SQL Server

```

---

## 5. Vai trò của từng tầng

### View (Thymeleaf)

- Hiển thị dữ liệu
- Gửi form
- Không xử lý nghiệp vụ

### Controller

- Nhận request
- Validate dữ liệu
- Gọi DAO để xử lý

### Model

#### Entities
- Biểu diễn các bảng trong database

#### DAO
- Truy cập dữ liệu
- Kế thừa JpaRepository

---

## 6. Nguyên tắc thiết kế

- View không gọi trực tiếp database
- Controller không chứa SQL
- Entity chỉ chứa mapping
- DAO chỉ lo truy vấn
- Mọi nghiệp vụ mượn – trả – bán đều đi qua Controller → DAO

---

## 7. Tổng kết kiến trúc

```

Thymeleaf
↓
Controller
↓
DAO
↓
Entity
↓
SQL Server


