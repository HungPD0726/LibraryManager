### Create database query
/* =========================================================
LIBRARY MANAGER - FULL CLEAN SCRIPT
DB: LibraryManager_V2
SQL Server
========================================================= */

-- 1. Create new database
IF DB_ID(N'LibraryManager_V2') IS NOT NULL
BEGIN
ALTER DATABASE LibraryManager_V2 SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
DROP DATABASE LibraryManager_V2;
END
GO

CREATE DATABASE LibraryManager_V2;
GO

USE LibraryManager_V2;
GO

/* =========================================================
ROLE & STAFF (N-N)
========================================================= */

CREATE TABLE Role (
RoleID   INT IDENTITY PRIMARY KEY,
RoleName NVARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE Staff (
StaffID   INT IDENTITY PRIMARY KEY,
StaffName NVARCHAR(100) NOT NULL
);

CREATE TABLE StaffRole (
StaffID INT NOT NULL,
RoleID  INT NOT NULL,

    CONSTRAINT PK_StaffRole PRIMARY KEY (StaffID, RoleID),
    CONSTRAINT FK_StaffRole_Staff FOREIGN KEY (StaffID) REFERENCES Staff(StaffID),
    CONSTRAINT FK_StaffRole_Role  FOREIGN KEY (RoleID)  REFERENCES Role(RoleID)
);

/* =========================================================
STUDENT
========================================================= */

CREATE TABLE Student (
StudentID   INT IDENTITY PRIMARY KEY,
StudentName NVARCHAR(100) NOT NULL,
Email       NVARCHAR(100) UNIQUE,
Phone       NVARCHAR(20)
);

/* =========================================================
MASTER DATA
========================================================= */

CREATE TABLE Category (
CategoryID   INT IDENTITY PRIMARY KEY,
CategoryName NVARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE Publisher (
PublisherID   INT IDENTITY PRIMARY KEY,
PublisherName NVARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE Author (
AuthorID   INT IDENTITY PRIMARY KEY,
AuthorName NVARCHAR(100) NOT NULL
);

/* =========================================================
BOOK
========================================================= */

CREATE TABLE Book (
BookID      INT IDENTITY PRIMARY KEY,
BookName    NVARCHAR(200) NOT NULL,
Quantity    INT NOT NULL CHECK (Quantity >= 0),
Available   INT NOT NULL CHECK (Available >= 0),
CategoryID  INT NOT NULL,
PublisherID INT NOT NULL,

    CONSTRAINT FK_Book_Category  FOREIGN KEY (CategoryID)  REFERENCES Category(CategoryID),
    CONSTRAINT FK_Book_Publisher FOREIGN KEY (PublisherID) REFERENCES Publisher(PublisherID),
    CONSTRAINT CK_Book_Available CHECK (Available <= Quantity)
);

/* Book - Author (N-N) */
CREATE TABLE BookAuthor (
BookID   INT NOT NULL,
AuthorID INT NOT NULL,

    CONSTRAINT PK_BookAuthor PRIMARY KEY (BookID, AuthorID),
    CONSTRAINT FK_BookAuthor_Book   FOREIGN KEY (BookID)   REFERENCES Book(BookID),
    CONSTRAINT FK_BookAuthor_Author FOREIGN KEY (AuthorID) REFERENCES Author(AuthorID)
);

/* =========================================================
BOOK CODE (1-N)
========================================================= */

CREATE TABLE BookCode (
BookCodeID INT IDENTITY PRIMARY KEY,
BookID     INT NOT NULL,
BookCode   NVARCHAR(50) NOT NULL UNIQUE,

    CONSTRAINT FK_BookCode_Book FOREIGN KEY (BookID) REFERENCES Book(BookID)
);

/* =========================================================
PRICE (N-N với Book)
========================================================= */

CREATE TABLE Price (
PriceID  INT IDENTITY PRIMARY KEY,
Amount   DECIMAL(12,2) NOT NULL CHECK (Amount > 0),
Currency NVARCHAR(10) DEFAULT 'VND',
Note     NVARCHAR(200)
);

CREATE TABLE BookPrice (
BookID    INT NOT NULL,
PriceID   INT NOT NULL,
StartDate DATE NOT NULL,
EndDate   DATE NULL,

    CONSTRAINT PK_BookPrice PRIMARY KEY (BookID, PriceID, StartDate),
    CONSTRAINT FK_BookPrice_Book  FOREIGN KEY (BookID)  REFERENCES Book(BookID),
    CONSTRAINT FK_BookPrice_Price FOREIGN KEY (PriceID) REFERENCES Price(PriceID),
    CONSTRAINT CK_BookPrice_Date CHECK (EndDate IS NULL OR EndDate >= StartDate)
);

/* =========================================================
BORROW
========================================================= */

CREATE TABLE Borrow (
BorrowID   INT IDENTITY PRIMARY KEY,
StudentID  INT NOT NULL,
StaffID    INT NOT NULL,
BorrowDate DATE NOT NULL,
DueDate    DATE NOT NULL,
Status     NVARCHAR(20) NOT NULL
CHECK (Status IN ('Borrowing','Returned','Overdue')),

    CONSTRAINT FK_Borrow_Student FOREIGN KEY (StudentID) REFERENCES Student(StudentID),
    CONSTRAINT FK_Borrow_Staff   FOREIGN KEY (StaffID)   REFERENCES Staff(StaffID),
    CONSTRAINT CK_Borrow_Date CHECK (DueDate >= BorrowDate)
);

CREATE TABLE BorrowItem (
BorrowID INT NOT NULL,
BookID   INT NOT NULL,
Quantity INT NOT NULL CHECK (Quantity > 0),

    CONSTRAINT PK_BorrowItem PRIMARY KEY (BorrowID, BookID),
    CONSTRAINT FK_BorrowItem_Borrow FOREIGN KEY (BorrowID) REFERENCES Borrow(BorrowID),
    CONSTRAINT FK_BorrowItem_Book   FOREIGN KEY (BookID)   REFERENCES Book(BookID)
);

/* =========================================================
ORDERS (SELL BOOK)
========================================================= */

CREATE TABLE Orders (
OrderID     INT IDENTITY PRIMARY KEY,
StudentID   INT NOT NULL,
StaffID     INT NOT NULL,
OrderDate   DATE NOT NULL,
TotalAmount DECIMAL(12,2) NOT NULL CHECK (TotalAmount >= 0),
Status      NVARCHAR(20),

    CONSTRAINT FK_Orders_Student FOREIGN KEY (StudentID) REFERENCES Student(StudentID),
    CONSTRAINT FK_Orders_Staff   FOREIGN KEY (StaffID)   REFERENCES Staff(StaffID)
);

CREATE TABLE OrderDetail (
OrderID   INT NOT NULL,
BookID    INT NOT NULL,
Quantity  INT NOT NULL CHECK (Quantity > 0),
UnitPrice DECIMAL(12,2) NOT NULL CHECK (UnitPrice > 0),

    CONSTRAINT PK_OrderDetail PRIMARY KEY (OrderID, BookID),
    CONSTRAINT FK_OrderDetail_Order FOREIGN KEY (OrderID) REFERENCES Orders(OrderID),
    CONSTRAINT FK_OrderDetail_Book  FOREIGN KEY (BookID)  REFERENCES Book(BookID)
);
--create table bookfile

USE LibraryManager_V2;
GO

-- Thêm bảng BookFile: 1 Book có nhiều file, 1 Staff upload nhiều file
IF OBJECT_ID(N'dbo.BookFile', N'U') IS NULL
BEGIN
CREATE TABLE dbo.BookFile (
BookFileID INT IDENTITY(1,1) PRIMARY KEY,
BookID     INT NOT NULL,
StaffID    INT NOT NULL,

        FileName   NVARCHAR(255) NOT NULL,
        FileUrl    NVARCHAR(500) NOT NULL,   -- hoặc FilePath
        FileType   NVARCHAR(50)  NULL,       -- pdf, epub, image...
        FileSize   BIGINT NULL CHECK (FileSize IS NULL OR FileSize >= 0),
        UploadAt   DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        IsActive   BIT NOT NULL DEFAULT 1,

        CONSTRAINT FK_BookFile_Book
            FOREIGN KEY (BookID) REFERENCES dbo.Book(BookID),

        CONSTRAINT FK_BookFile_Staff
            FOREIGN KEY (StaffID) REFERENCES dbo.Staff(StaffID)
    );

    CREATE INDEX IX_BookFile_BookID  ON dbo.BookFile(BookID);
    CREATE INDEX IX_BookFile_StaffID ON dbo.BookFile(StaffID);
END
GO



