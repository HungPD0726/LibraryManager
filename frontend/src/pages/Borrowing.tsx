import { useState } from "react";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Plus, CheckCircle } from "lucide-react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { fetchBorrows, fetchBooks, fetchStudents, fetchStaff, createBorrow, returnBorrow } from "@/services/api";
import type { BorrowRecord, Book, Student } from "@/types/types";

const statusLabel: Record<string, string> = { Borrowing: "Đang mượn", Returned: "Đã trả", Overdue: "Quá hạn" };
const statusVariant: Record<string, "default" | "secondary" | "destructive"> = { Borrowing: "default", Returned: "secondary", Overdue: "destructive" };

const Borrowing = () => {
  const [dialogOpen, setDialogOpen] = useState(false);
  const [tab, setTab] = useState("Borrowing");
  const [selectedStudentId, setSelectedStudentId] = useState("");
  const [selectedBookId, setSelectedBookId] = useState("");
  const [dueDate, setDueDate] = useState("");
  const queryClient = useQueryClient();

  const { data: borrows = [] } = useQuery({ queryKey: ["borrows"], queryFn: fetchBorrows });
  const { data: books = [] } = useQuery({ queryKey: ["books"], queryFn: fetchBooks });
  const { data: students = [] } = useQuery({ queryKey: ["students"], queryFn: fetchStudents });
  const { data: staff = [] } = useQuery({ queryKey: ["staff"], queryFn: fetchStaff });

  const createMutation = useMutation({
    mutationFn: createBorrow,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["borrows"] });
      queryClient.invalidateQueries({ queryKey: ["books"] });
      setDialogOpen(false);
    },
  });

  const returnMutation = useMutation({
    mutationFn: returnBorrow,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["borrows"] });
      queryClient.invalidateQueries({ queryKey: ["books"] });
    },
  });

  const filtered = borrows.filter((r: BorrowRecord) => {
    if (tab === "all") return true;
    return r.status === tab;
  });

  const handleCreate = () => {
    if (!selectedStudentId || !selectedBookId || !dueDate) return;
    createMutation.mutate({
      studentId: parseInt(selectedStudentId),
      staffId: staff[0]?.staffId || 1,
      dueDate,
      items: [{ bookId: parseInt(selectedBookId), quantity: 1 }],
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Mượn / Trả Sách</h1>
          <p className="text-muted-foreground">{borrows.filter((r: BorrowRecord) => r.status === "Borrowing").length} phiếu đang hoạt động</p>
        </div>
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger asChild>
            <Button><Plus className="mr-2 h-4 w-4" />Tạo phiếu mượn</Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader><DialogTitle>Tạo phiếu mượn mới</DialogTitle></DialogHeader>
            <div className="grid gap-4 py-4">
              <div className="space-y-2"><Label>Thành viên</Label>
                <Select onValueChange={setSelectedStudentId}>
                  <SelectTrigger><SelectValue placeholder="Chọn thành viên" /></SelectTrigger>
                  <SelectContent>{students.map((m: Student) => <SelectItem key={m.studentId} value={m.studentId.toString()}>{m.studentName}</SelectItem>)}</SelectContent>
                </Select>
              </div>
              <div className="space-y-2"><Label>Sách</Label>
                <Select onValueChange={setSelectedBookId}>
                  <SelectTrigger><SelectValue placeholder="Chọn sách" /></SelectTrigger>
                  <SelectContent>{books.filter((b: Book) => b.available > 0).map((b: Book) => <SelectItem key={b.bookId} value={b.bookId.toString()}>{b.bookName}</SelectItem>)}</SelectContent>
                </Select>
              </div>
              <div className="space-y-2"><Label>Ngày hẹn trả</Label><Input type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)} /></div>
              <Button onClick={handleCreate} disabled={createMutation.isPending}>
                {createMutation.isPending ? "Đang tạo..." : "Tạo phiếu"}
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      <Card>
        <CardHeader className="pb-3">
          <Tabs value={tab} onValueChange={setTab}>
            <TabsList>
              <TabsTrigger value="Borrowing">Đang mượn</TabsTrigger>
              <TabsTrigger value="Overdue">Quá hạn</TabsTrigger>
              <TabsTrigger value="Returned">Đã trả</TabsTrigger>
              <TabsTrigger value="all">Tất cả</TabsTrigger>
            </TabsList>
          </Tabs>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Mã</TableHead>
                <TableHead>Sách</TableHead>
                <TableHead>Người mượn</TableHead>
                <TableHead>Ngày mượn</TableHead>
                <TableHead>Hạn trả</TableHead>
                <TableHead>Trạng thái</TableHead>
                <TableHead className="text-right">Thao tác</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filtered.map((r: BorrowRecord) => (
                <TableRow key={r.borrowId}>
                  <TableCell className="font-mono text-xs">{r.borrowId}</TableCell>
                  <TableCell className="font-medium">{r.items?.map((i) => i.bookName).join(", ") || "—"}</TableCell>
                  <TableCell>{r.studentName}</TableCell>
                  <TableCell>{r.borrowDate}</TableCell>
                  <TableCell>{r.dueDate}</TableCell>
                  <TableCell>
                    <Badge variant={statusVariant[r.status] || "default"}>
                      {statusLabel[r.status] || r.status}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    {r.status !== "Returned" && (
                      <Button variant="outline" size="sm" onClick={() => returnMutation.mutate(r.borrowId)} disabled={returnMutation.isPending}>
                        <CheckCircle className="mr-1 h-4 w-4" />Trả sách
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
              {filtered.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} className="text-center text-muted-foreground py-8">Không có dữ liệu</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
};

export default Borrowing;
