import { useState } from "react";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Search, Plus, Edit, Trash2, Eye } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { fetchBooks, fetchCategories, createBook, deleteBook, fetchPublishers } from "@/services/api";
import type { Book } from "@/types/types";

const statusLabel = { available: "Có sẵn", low: "Sắp hết", out: "Hết" };
const statusVariant = { available: "default" as const, low: "secondary" as const, out: "destructive" as const };

const Books = () => {
  const [search, setSearch] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("all");
  const [dialogOpen, setDialogOpen] = useState(false);
  const [newBook, setNewBook] = useState({ bookName: "", categoryId: 0, publisherId: 0, quantity: 0 });
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: books = [] } = useQuery({ queryKey: ["books"], queryFn: fetchBooks });
  const { data: categories = [] } = useQuery({ queryKey: ["categories"], queryFn: fetchCategories });
  const { data: publishers = [] } = useQuery({ queryKey: ["publishers"], queryFn: fetchPublishers });

  const createMutation = useMutation({
    mutationFn: createBook,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["books"] });
      setDialogOpen(false);
      setNewBook({ bookName: "", categoryId: 0, publisherId: 0, quantity: 0 });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteBook,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["books"] }),
  });

  const filtered = books.filter((b: Book) => {
    const matchSearch = b.bookName.toLowerCase().includes(search.toLowerCase()) ||
      (b.authors?.some((a) => a.authorName.toLowerCase().includes(search.toLowerCase())));
    const matchCategory = categoryFilter === "all" || b.categoryId?.toString() === categoryFilter;
    return matchSearch && matchCategory;
  });

  const handleCreate = () => {
    if (!newBook.bookName || !newBook.categoryId || !newBook.publisherId) return;
    createMutation.mutate(newBook);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Quản lý Sách</h1>
          <p className="text-muted-foreground">Tổng cộng {books.length} đầu sách</p>
        </div>
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger asChild>
            <Button><Plus className="mr-2 h-4 w-4" />Thêm sách</Button>
          </DialogTrigger>
          <DialogContent className="max-w-lg">
            <DialogHeader><DialogTitle>Thêm sách mới</DialogTitle></DialogHeader>
            <div className="grid gap-4 py-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Tên sách</Label>
                  <Input placeholder="Nhập tên sách" value={newBook.bookName} onChange={(e) => setNewBook({ ...newBook, bookName: e.target.value })} />
                </div>
                <div className="space-y-2">
                  <Label>Số lượng</Label>
                  <Input type="number" placeholder="0" value={newBook.quantity || ""} onChange={(e) => setNewBook({ ...newBook, quantity: parseInt(e.target.value) || 0 })} />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Thể loại</Label>
                  <Select onValueChange={(v) => setNewBook({ ...newBook, categoryId: parseInt(v) })}>
                    <SelectTrigger><SelectValue placeholder="Chọn thể loại" /></SelectTrigger>
                    <SelectContent>
                      {categories.map((c) => <SelectItem key={c.categoryId} value={c.categoryId.toString()}>{c.categoryName}</SelectItem>)}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label>Nhà xuất bản</Label>
                  <Select onValueChange={(v) => setNewBook({ ...newBook, publisherId: parseInt(v) })}>
                    <SelectTrigger><SelectValue placeholder="Chọn NXB" /></SelectTrigger>
                    <SelectContent>
                      {publishers.map((p) => <SelectItem key={p.publisherId} value={p.publisherId.toString()}>{p.publisherName}</SelectItem>)}
                    </SelectContent>
                  </Select>
                </div>
              </div>
              <Button onClick={handleCreate} disabled={createMutation.isPending}>
                {createMutation.isPending ? "Đang lưu..." : "Lưu sách"}
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      <Card>
        <CardHeader className="pb-3">
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input placeholder="Tìm theo tên sách hoặc tác giả..." value={search} onChange={(e) => setSearch(e.target.value)} className="pl-9" />
            </div>
            <Select value={categoryFilter} onValueChange={setCategoryFilter}>
              <SelectTrigger className="w-full sm:w-48"><SelectValue placeholder="Thể loại" /></SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Tất cả</SelectItem>
                {categories.map((c) => <SelectItem key={c.categoryId} value={c.categoryId.toString()}>{c.categoryName}</SelectItem>)}
              </SelectContent>
            </Select>
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Mã</TableHead>
                <TableHead>Tên sách</TableHead>
                <TableHead>Tác giả</TableHead>
                <TableHead>Thể loại</TableHead>
                <TableHead className="text-center">SL / Có sẵn</TableHead>
                <TableHead>Trạng thái</TableHead>
                <TableHead className="text-right">Thao tác</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filtered.map((book: Book) => (
                <TableRow key={book.bookId}>
                  <TableCell className="font-mono text-xs">{book.bookId}</TableCell>
                  <TableCell className="font-medium">{book.bookName}</TableCell>
                  <TableCell>{book.authors?.map((a) => a.authorName).join(", ") || "—"}</TableCell>
                  <TableCell><Badge variant="outline">{book.categoryName}</Badge></TableCell>
                  <TableCell className="text-center">{book.quantity} / {book.available}</TableCell>
                  <TableCell><Badge variant={statusVariant[book.status]}>{statusLabel[book.status]}</Badge></TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-1">
                      <Button variant="ghost" size="icon" onClick={() => navigate(`/books/${book.bookId}`)}><Eye className="h-4 w-4" /></Button>
                      <Button variant="ghost" size="icon"><Edit className="h-4 w-4" /></Button>
                      <Button variant="ghost" size="icon" onClick={() => deleteMutation.mutate(book.bookId)}><Trash2 className="h-4 w-4 text-destructive" /></Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
              {filtered.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} className="text-center text-muted-foreground py-8">Không có sách nào</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
};

export default Books;
