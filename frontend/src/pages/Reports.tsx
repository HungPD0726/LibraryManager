import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { BookOpen, Users, ArrowLeftRight, ShoppingCart } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { fetchDashboardStats, fetchOrders } from "@/services/api";
import type { OrderRecord } from "@/types/types";

const Reports = () => {
  const { data: stats } = useQuery({ queryKey: ["dashboardStats"], queryFn: fetchDashboardStats });
  const { data: orders = [] } = useQuery({ queryKey: ["orders"], queryFn: fetchOrders });

  const totalRevenue = orders.reduce((s: number, r: OrderRecord) => s + (r.totalAmount || 0), 0);

  const reportCards = [
    { label: "Tổng số sách", value: stats?.totalBooks ?? 0, icon: BookOpen, color: "text-primary" },
    { label: "Tổng thành viên", value: stats?.totalStudents ?? 0, icon: Users, color: "text-accent" },
    { label: "Phiếu mượn hoạt động", value: stats?.activeBorrows ?? 0, icon: ArrowLeftRight, color: "text-success" },
    { label: "Tổng đơn bán", value: stats?.totalOrders ?? 0, icon: ShoppingCart, color: "text-primary" },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Báo cáo & Thống kê</h1>
        <p className="text-muted-foreground">Tổng quan hoạt động thư viện</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {reportCards.map((stat) => (
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

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Tổng doanh thu</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-4xl font-bold text-primary">{totalRevenue.toLocaleString()}đ</p>
          <p className="text-sm text-muted-foreground mt-2">Từ {orders.length} đơn hàng</p>
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader><CardTitle className="text-lg">Thể loại</CardTitle></CardHeader>
          <CardContent>
            <p className="text-sm text-muted-foreground">Tổng cộng {stats?.totalCategories ?? 0} thể loại sách</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle className="text-lg">Tình trạng mượn</CardTitle></CardHeader>
          <CardContent className="space-y-2">
            <div className="flex justify-between">
              <span className="text-sm">Đang mượn</span>
              <span className="font-medium">{stats?.activeBorrows ?? 0}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-sm text-destructive">Quá hạn</span>
              <span className="font-medium text-destructive">{stats?.overdueBorrows ?? 0}</span>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Reports;
