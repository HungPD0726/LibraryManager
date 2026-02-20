// TypeScript types matching backend API responses

export interface Book {
  bookId: number;
  bookName: string;
  quantity: number;
  available: number;
  categoryId: number;
  categoryName: string;
  publisherId: number;
  publisherName: string;
  authors: Author[];
  status: "available" | "low" | "out";
}

export interface Author {
  authorId: number;
  authorName: string;
}

export interface Category {
  categoryId: number;
  categoryName: string;
}

export interface Publisher {
  publisherId: number;
  publisherName: string;
}

export interface Student {
  studentId: number;
  studentName: string;
  email: string;
  phone: string;
}

export interface Staff {
  staffId: number;
  staffName: string;
}

export interface BorrowItem {
  bookId: number;
  bookName: string;
  quantity: number;
}

export interface BorrowRecord {
  borrowId: number;
  studentId: number;
  studentName: string;
  staffId: number;
  staffName: string;
  borrowDate: string;
  dueDate: string;
  status: string; // Borrowing, Returned, Overdue
  items: BorrowItem[];
}

export interface OrderDetailItem {
  bookId: number;
  bookName: string;
  quantity: number;
  unitPrice: number;
  total: number;
}

export interface OrderRecord {
  orderId: number;
  studentId: number;
  studentName: string;
  staffId: number;
  orderDate: string;
  totalAmount: number;
  status: string;
  items: OrderDetailItem[];
}

export interface DashboardStats {
  totalBooks: number;
  totalStudents: number;
  activeBorrows: number;
  overdueBorrows: number;
  totalOrders: number;
  totalCategories: number;
}
