import { useParams, useNavigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ArrowLeft, BookOpen } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { fetchBookById } from "@/services/api";

const statusLabel: Record<string, string> = { available: "Có sẵn", low: "Sắp hết", out: "Hết" };
const statusVariant: Record<string, "default" | "secondary" | "destructive"> = { available: "default", low: "secondary", out: "destructive" };

const BookDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const { data: book, isLoading } = useQuery({
    queryKey: ["book", id],
    queryFn: () => fetchBookById(parseInt(id || "0")),
    enabled: !!id,
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-20">
        <p className="text-muted-foreground">Đang tải...</p>
      </div>
    );
  }

  if (!book) {
    return (
      <div className="text-center py-20">
        <h2 className="text-xl font-semibold">Không tìm thấy sách</h2>
        <Button variant="outline" className="mt-4" onClick={() => navigate("/books")}>
          <ArrowLeft className="mr-2 h-4 w-4" />Quay lại
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <Button variant="ghost" onClick={() => navigate("/books")}>
        <ArrowLeft className="mr-2 h-4 w-4" />Quay lại danh sách
      </Button>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-2">
          <CardHeader>
            <div className="flex items-start justify-between">
              <div>
                <CardTitle className="text-2xl">{book.bookName}</CardTitle>
                <p className="text-muted-foreground mt-1">
                  {book.authors?.map((a) => a.authorName).join(", ") || "Chưa có tác giả"}
                </p>
              </div>
              <Badge variant={statusVariant[book.status]}>{statusLabel[book.status]}</Badge>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-muted-foreground">Thể loại</p>
                <p className="font-medium">{book.categoryName || "—"}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Nhà xuất bản</p>
                <p className="font-medium">{book.publisherName || "—"}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Tổng số lượng</p>
                <p className="font-medium">{book.quantity}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Có sẵn</p>
                <p className="font-medium">{book.available}</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <BookOpen className="h-5 w-5" />
              Thông tin thêm
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div>
              <p className="text-sm text-muted-foreground">Mã sách</p>
              <p className="font-mono">{book.bookId}</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Đang được mượn</p>
              <p className="font-medium">{book.quantity - book.available} cuốn</p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default BookDetail;
