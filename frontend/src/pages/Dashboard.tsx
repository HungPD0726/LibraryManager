import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { BookOpen, Users, ArrowLeftRight, AlertTriangle, TrendingUp, Clock } from "lucide-react";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useQuery } from "@tanstack/react-query";
import { fetchDashboardStats, fetchBorrows } from "@/services/api";

const Dashboard = () => {
  const { data: stats } = useQuery({
    queryKey: ["dashboardStats"],
    queryFn: fetchDashboardStats,
  });

  const { data: borrows } = useQuery({
    queryKey: ["borrows"],
    queryFn: fetchBorrows,
  });

  const activeBorrows = borrows?.filter((r) => r.status === "Borrowing") ?? [];
  const overdueBorrows = borrows?.filter((r) => r.status === "Overdue") ?? [];

  const upcomingDue = activeBorrows
    .sort((a, b) => new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime())
    .slice(0, 5);

  const statCards = [
    { label: "Tổng số sách", value: stats?.totalBooks ?? 0, icon: BookOpen, color: "text-primary" },
    { label: "Thành viên", value: stats?.totalStudents ?? 0, icon: Users, color: "text-accent" },
    { label: "Đang mượn", value: stats?.activeBorrows ?? 0, icon: ArrowLeftRight, color: "text-success" },
    { label: "Quá hạn", value: stats?.overdueBorrows ?? 0, icon: AlertTriangle, color: "text-destructive" },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-foreground">Tổng quan</h1>
        <p className="text-muted-foreground">Chào mừng bạn trở lại hệ thống quản lý thư viện</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {statCards.map((stat) => (
          <Card key={stat.label}>
            <CardContent className="p-5">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">{stat.label}</p>
                  <p className="text-3xl font-bold mt-1">{stat.value}</p>
                </div>
                <div className={`h-12 w-12 rounded-lg bg-muted flex items-center justify-center ${stat.color}`}>
                  <stat.icon className="h-6 w-6" />
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <Clock className="h-5 w-5" />
              Sắp đến hạn trả
            </CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Sách</TableHead>
                  <TableHead>Người mượn</TableHead>
                  <TableHead>Ngày mượn</TableHead>
                  <TableHead>Hạn trả</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {upcomingDue.map((record) => (
                  <TableRow key={record.borrowId}>
                    <TableCell className="font-medium">
                      {record.items?.map((i) => i.bookName).join(", ") || "—"}
                    </TableCell>
                    <TableCell>{record.studentName}</TableCell>
                    <TableCell>{record.borrowDate}</TableCell>
                    <TableCell>{record.dueDate}</TableCell>
                  </TableRow>
                ))}
                {upcomingDue.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={4} className="text-center text-muted-foreground">Không có dữ liệu</TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <AlertTriangle className="h-5 w-5 text-destructive" />
              Sách quá hạn ({overdueBorrows.length})
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {overdueBorrows.map((record) => (
              <div key={record.borrowId} className="flex items-start justify-between p-3 rounded-lg bg-destructive/5 border border-destructive/10">
                <div>
                  <p className="text-sm font-medium">
                    {record.items?.map((i) => i.bookName).join(", ") || "—"}
                  </p>
                  <p className="text-xs text-muted-foreground">{record.studentName}</p>
                  <p className="text-xs text-destructive">Hạn: {record.dueDate}</p>
                </div>
              </div>
            ))}
            {overdueBorrows.length === 0 && (
              <p className="text-sm text-muted-foreground text-center py-4">Không có sách quá hạn</p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Dashboard;
