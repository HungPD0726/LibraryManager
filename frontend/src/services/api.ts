import type {
  Book,
  Category,
  Publisher,
  Student,
  Staff,
  BorrowRecord,
  OrderRecord,
  DashboardStats,
} from "@/types/types";

const API_BASE = "/api";

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${url}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (!res.ok) {
    const errorBody = await res.text();
    throw new Error(`API error ${res.status}: ${errorBody}`);
  }
  return res.json();
}

// === Books ===
export const fetchBooks = () => request<Book[]>("/books");

export const fetchBookById = (id: number) => request<Book>(`/books/${id}`);

export const createBook = (data: {
  bookName: string;
  quantity: number;
  categoryId: number;
  publisherId: number;
  authorIds?: number[];
}) =>
  request<Book>("/books", {
    method: "POST",
    body: JSON.stringify({ ...data, available: data.quantity }),
  });

export const updateBook = (id: number, data: Record<string, unknown>) =>
  request<Book>(`/books/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const deleteBook = (id: number) =>
  request<void>(`/books/${id}`, { method: "DELETE" });

// === Categories ===
export const fetchCategories = () => request<Category[]>("/categories");

export const createCategory = (categoryName: string) =>
  request<Category>("/categories", {
    method: "POST",
    body: JSON.stringify({ categoryName }),
  });

// === Publishers ===
export const fetchPublishers = () => request<Publisher[]>("/publishers");

export const createPublisher = (publisherName: string) =>
  request<Publisher>("/publishers", {
    method: "POST",
    body: JSON.stringify({ publisherName }),
  });

// === Students (Members) ===
export const fetchStudents = () => request<Student[]>("/students");

export const fetchStudentById = (id: number) =>
  request<Student>(`/students/${id}`);

export const createStudent = (data: {
  studentName: string;
  email: string;
  phone: string;
}) =>
  request<Student>("/students", {
    method: "POST",
    body: JSON.stringify(data),
  });

export const updateStudent = (
  id: number,
  data: { studentName: string; email: string; phone: string }
) =>
  request<Student>(`/students/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const deleteStudent = (id: number) =>
  request<void>(`/students/${id}`, { method: "DELETE" });

// === Staff ===
export const fetchStaff = () => request<Staff[]>("/staff");

// === Borrows ===
export const fetchBorrows = () => request<BorrowRecord[]>("/borrows");

export const createBorrow = (data: {
  studentId: number;
  staffId: number;
  dueDate: string;
  items: { bookId: number; quantity: number }[];
}) =>
  request<BorrowRecord>("/borrows", {
    method: "POST",
    body: JSON.stringify(data),
  });

export const returnBorrow = (id: number) =>
  request<BorrowRecord>(`/borrows/${id}/return`, { method: "PUT" });

// === Orders ===
export const fetchOrders = () => request<OrderRecord[]>("/orders");

export const createOrder = (data: {
  studentId: number;
  staffId: number;
  items: { bookId: number; quantity: number; unitPrice: number }[];
}) =>
  request<OrderRecord>("/orders", {
    method: "POST",
    body: JSON.stringify(data),
  });

// === Dashboard ===
export const fetchDashboardStats = () =>
  request<DashboardStats>("/dashboard/stats");
