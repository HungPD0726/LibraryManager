// Mock data for Library Management System

export interface Book {
  id: string;
  title: string;
  author: string;
  category: string;
  isbn: string;
  quantity: number;
  available: number;
  coverUrl?: string;
  status: "available" | "low" | "out";
  price?: number;
  forSale: boolean;
}

export interface Member {
  id: string;
  name: string;
  class: string;
  email: string;
  phone: string;
  joinDate: string;
  borrowedCount: number;
}

export interface BorrowRecord {
  id: string;
  memberId: string;
  memberName: string;
  bookId: string;
  bookTitle: string;
  borrowDate: string;
  dueDate: string;
  returnDate?: string;
  status: "active" | "returned" | "overdue";
  fine?: number;
}

export interface SaleRecord {
  id: string;
  bookId: string;
  bookTitle: string;
  buyerName: string;
  quantity: number;
  price: number;
  total: number;
  date: string;
}

export const books: Book[] = [
  { id: "B001", title: "Toán Cao Cấp", author: "Nguyễn Văn A", category: "Giáo khoa", isbn: "978-604-1-00001", quantity: 20, available: 15, status: "available", price: 85000, forSale: true },
  { id: "B002", title: "Vật Lý Đại Cương", author: "Trần Thị B", category: "Giáo khoa", isbn: "978-604-1-00002", quantity: 15, available: 3, status: "low", price: 72000, forSale: true },
  { id: "B003", title: "Lịch Sử Việt Nam", author: "Lê Văn C", category: "Lịch sử", isbn: "978-604-1-00003", quantity: 10, available: 0, status: "out", forSale: false },
  { id: "B004", title: "Tiếng Anh Giao Tiếp", author: "Phạm Thị D", category: "Ngoại ngữ", isbn: "978-604-1-00004", quantity: 25, available: 18, status: "available", price: 65000, forSale: true },
  { id: "B005", title: "Hóa Học Hữu Cơ", author: "Hoàng Văn E", category: "Giáo khoa", isbn: "978-604-1-00005", quantity: 12, available: 7, status: "available", forSale: false },
  { id: "B006", title: "Sinh Học Phân Tử", author: "Đỗ Thị F", category: "Giáo khoa", isbn: "978-604-1-00006", quantity: 8, available: 2, status: "low", price: 90000, forSale: true },
  { id: "B007", title: "Văn Học Việt Nam Hiện Đại", author: "Vũ Văn G", category: "Văn học", isbn: "978-604-1-00007", quantity: 18, available: 12, status: "available", forSale: false },
  { id: "B008", title: "Tin Học Cơ Bản", author: "Ngô Thị H", category: "Tin học", isbn: "978-604-1-00008", quantity: 30, available: 22, status: "available", price: 55000, forSale: true },
  { id: "B009", title: "Địa Lý Tự Nhiên", author: "Bùi Văn I", category: "Địa lý", isbn: "978-604-1-00009", quantity: 6, available: 0, status: "out", forSale: false },
  { id: "B010", title: "Triết Học Mác-Lênin", author: "Đặng Thị K", category: "Chính trị", isbn: "978-604-1-00010", quantity: 14, available: 9, status: "available", price: 45000, forSale: true },
];

export const members: Member[] = [
  { id: "M001", name: "Nguyễn Minh Anh", class: "12A1", email: "minhanh@school.edu.vn", phone: "0901234567", joinDate: "2025-09-01", borrowedCount: 5 },
  { id: "M002", name: "Trần Đức Bình", class: "11B2", email: "ducbinh@school.edu.vn", phone: "0912345678", joinDate: "2025-09-05", borrowedCount: 3 },
  { id: "M003", name: "Lê Thị Cẩm", class: "10A3", email: "thicam@school.edu.vn", phone: "0923456789", joinDate: "2025-09-10", borrowedCount: 8 },
  { id: "M004", name: "Phạm Quốc Đạt", class: "12A2", email: "quocdat@school.edu.vn", phone: "0934567890", joinDate: "2025-10-01", borrowedCount: 2 },
  { id: "M005", name: "Hoàng Mai Lan", class: "11A1", email: "mailan@school.edu.vn", phone: "0945678901", joinDate: "2025-10-15", borrowedCount: 6 },
  { id: "M006", name: "Vũ Thanh Hải", class: "10B1", email: "thanhhai@school.edu.vn", phone: "0956789012", joinDate: "2025-11-01", borrowedCount: 1 },
];

export const borrowRecords: BorrowRecord[] = [
  { id: "BR001", memberId: "M001", memberName: "Nguyễn Minh Anh", bookId: "B001", bookTitle: "Toán Cao Cấp", borrowDate: "2026-01-10", dueDate: "2026-02-10", status: "active" },
  { id: "BR002", memberId: "M003", memberName: "Lê Thị Cẩm", bookId: "B003", bookTitle: "Lịch Sử Việt Nam", borrowDate: "2026-01-05", dueDate: "2026-01-20", status: "overdue", fine: 15000 },
  { id: "BR003", memberId: "M002", memberName: "Trần Đức Bình", bookId: "B005", bookTitle: "Hóa Học Hữu Cơ", borrowDate: "2026-01-15", dueDate: "2026-02-15", status: "active" },
  { id: "BR004", memberId: "M005", memberName: "Hoàng Mai Lan", bookId: "B002", bookTitle: "Vật Lý Đại Cương", borrowDate: "2025-12-20", dueDate: "2026-01-20", status: "overdue", fine: 30000 },
  { id: "BR005", memberId: "M001", memberName: "Nguyễn Minh Anh", bookId: "B007", bookTitle: "Văn Học Việt Nam Hiện Đại", borrowDate: "2026-01-20", dueDate: "2026-02-20", status: "active" },
  { id: "BR006", memberId: "M004", memberName: "Phạm Quốc Đạt", bookId: "B004", bookTitle: "Tiếng Anh Giao Tiếp", borrowDate: "2025-12-01", dueDate: "2025-12-31", returnDate: "2025-12-28", status: "returned" },
  { id: "BR007", memberId: "M006", memberName: "Vũ Thanh Hải", bookId: "B008", bookTitle: "Tin Học Cơ Bản", borrowDate: "2026-01-25", dueDate: "2026-02-25", status: "active" },
  { id: "BR008", memberId: "M003", memberName: "Lê Thị Cẩm", bookId: "B009", bookTitle: "Địa Lý Tự Nhiên", borrowDate: "2025-12-10", dueDate: "2026-01-10", status: "overdue", fine: 45000 },
];

export const saleRecords: SaleRecord[] = [
  { id: "S001", bookId: "B001", bookTitle: "Toán Cao Cấp", buyerName: "Nguyễn Văn X", quantity: 2, price: 85000, total: 170000, date: "2026-01-15" },
  { id: "S002", bookId: "B004", bookTitle: "Tiếng Anh Giao Tiếp", buyerName: "Trần Thị Y", quantity: 1, price: 65000, total: 65000, date: "2026-01-18" },
  { id: "S003", bookId: "B008", bookTitle: "Tin Học Cơ Bản", buyerName: "Lê Văn Z", quantity: 3, price: 55000, total: 165000, date: "2026-01-20" },
  { id: "S004", bookId: "B010", bookTitle: "Triết Học Mác-Lênin", buyerName: "Phạm Thị W", quantity: 1, price: 45000, total: 45000, date: "2026-02-01" },
  { id: "S005", bookId: "B006", bookTitle: "Sinh Học Phân Tử", buyerName: "Hoàng Văn V", quantity: 2, price: 90000, total: 180000, date: "2026-02-05" },
];

export const categories = ["Giáo khoa", "Lịch sử", "Ngoại ngữ", "Tin học", "Văn học", "Địa lý", "Chính trị"];

export const monthlyBorrowData = [
  { month: "T9", borrows: 45, returns: 38 },
  { month: "T10", borrows: 62, returns: 55 },
  { month: "T11", borrows: 58, returns: 50 },
  { month: "T12", borrows: 71, returns: 63 },
  { month: "T1", borrows: 53, returns: 42 },
  { month: "T2", borrows: 34, returns: 28 },
];

export const categoryStats = [
  { name: "Giáo khoa", value: 85 },
  { name: "Văn học", value: 42 },
  { name: "Ngoại ngữ", value: 35 },
  { name: "Tin học", value: 28 },
  { name: "Lịch sử", value: 22 },
  { name: "Khác", value: 15 },
];
