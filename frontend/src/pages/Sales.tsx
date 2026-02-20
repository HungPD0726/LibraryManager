import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Plus, ShoppingCart } from "lucide-react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { fetchOrders, fetchBooks, fetchStudents, fetchStaff, createOrder } from "@/services/api";
import type { OrderRecord, Book, Student } from "@/types/types";

const Sales = () => {
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedBookId, setSelectedBookId] = useState("");
  const [selectedStudentId, setSelectedStudentId] = useState("");
  const [quantity, setQuantity] = useState(1);
  const [unitPrice, setUnitPrice] = useState(0);
  const queryClient = useQueryClient();

  const { data: orders = [] } = useQuery({ queryKey: ["orders"], queryFn: fetchOrders });
  const { data: books = [] } = useQuery({ queryKey: ["books"], queryFn: fetchBooks });
  const { data: students = [] } = useQuery({ queryKey: ["students"], queryFn: fetchStudents });
  const { data: staff = [] } = useQuery({ queryKey: ["staff"], queryFn: fetchStaff });

  const createMutation = useMutation({
    mutationFn: createOrder,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["orders"] });
      queryClient.invalidateQueries({ queryKey: ["books"] });
      setDialogOpen(false);
    },
  });

  const totalRevenue = orders.reduce((s: number, r: OrderRecord) => s + (r.totalAmount || 0), 0);

  const handleCreate = () => {
    if (!selectedBookId || !selectedStudentId || !unitPrice) return;
    createMutation.mutate({
      studentId: parseInt(selectedStudentId),
      staffId: staff[0]?.staffId || 1,
      items: [{ bookId: parseInt(selectedBookId), quantity, unitPrice }],
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Bán Sách</h1>
          <p className="text-muted-foreground">Tổng doanh thu: {totalRevenue.toLocaleString()}đ</p>
        </div>
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger asChild>
            <Button><Plus className="mr-2 h-4 w-4" />Tạo đơn bán</Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader><DialogTitle>Tạo đơn bán sách</DialogTitle></DialogHeader>
            <div className="grid gap-4 py-4">
              <div className="space-y-2"><Label>Sách</Label>
                <Select onValueChange={setSelectedBookId}>
                  <SelectTrigger><SelectValue placeholder="Chọn sách" /></SelectTrigger>
                  <SelectContent>{books.filter((b: Book) => b.available > 0).map((b: Book) =>
                    <SelectItem key={b.bookId} value={b.bookId.toString()}>{b.bookName}</SelectItem>
                  )}</SelectContent>
                </Select>
              </div>
              <div className="space-y-2"><Label>Người mua</Label>
                <Select onValueChange={setSelectedStudentId}>
                  <SelectTrigger><SelectValue placeholder="Chọn người mua" /></SelectTrigger>
                  <SelectContent>{students.map((s: Student) =>
                    <SelectItem key={s.studentId} value={s.studentId.toString()}>{s.studentName}</SelectItem>
                  )}</SelectContent>
                </Select>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2"><Label>Số lượng</Label><Input type="number" value={quantity} onChange={(e) => setQuantity(parseInt(e.target.value) || 1)} /></div>
                <div className="space-y-2"><Label>Đơn giá (VNĐ)</Label><Input type="number" value={unitPrice || ""} onChange={(e) => setUnitPrice(parseInt(e.target.value) || 0)} /></div>
              </div>
              <Button onClick={handleCreate} disabled={createMutation.isPending}>
                {createMutation.isPending ? "Đang xử lý..." : "Xác nhận bán"}
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-2">
          <CardHeader><CardTitle className="text-lg">Lịch sử giao dịch</CardTitle></CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Mã</TableHead>
                  <TableHead>Sách</TableHead>
                  <TableHead>Người mua</TableHead>
                  <TableHead className="text-right">Thành tiền</TableHead>
                  <TableHead>Ngày</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {orders.map((r: OrderRecord) => (
                  <TableRow key={r.orderId}>
                    <TableCell className="font-mono text-xs">{r.orderId}</TableCell>
                    <TableCell className="font-medium">{r.items?.map((i) => i.bookName).join(", ") || "—"}</TableCell>
                    <TableCell>{r.studentName}</TableCell>
                    <TableCell className="text-right font-medium">{(r.totalAmount || 0).toLocaleString()}đ</TableCell>
                    <TableCell>{r.orderDate}</TableCell>
                  </TableRow>
                ))}
                {orders.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={5} className="text-center text-muted-foreground py-8">Chưa có giao dịch nào</TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle className="text-lg flex items-center gap-2"><ShoppingCart className="h-5 w-5" />Sách có sẵn</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            {books.filter((b: Book) => b.available > 0).map((b: Book) => (
              <div key={b.bookId} className="flex items-center justify-between p-3 rounded-lg border">
                <div>
                  <p className="text-sm font-medium">{b.bookName}</p>
                  <p className="text-xs text-muted-foreground">Còn: {b.available} cuốn</p>
                </div>
              </div>
            ))}
            {books.filter((b: Book) => b.available > 0).length === 0 && (
              <p className="text-sm text-muted-foreground text-center py-4">Không có sách nào</p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Sales;
