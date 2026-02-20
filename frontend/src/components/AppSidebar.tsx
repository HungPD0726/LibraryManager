import {
  BookOpen,
  LayoutDashboard,
  Users,
  ArrowLeftRight,
  ShoppingCart,
  BarChart3,
  Library,
} from "lucide-react";
import { NavLink } from "@/components/NavLink";
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarHeader,
  useSidebar,
} from "@/components/ui/sidebar";

const menuItems = [
  { title: "Tổng quan", url: "/dashboard", icon: LayoutDashboard },
  { title: "Quản lý Sách", url: "/books", icon: BookOpen },
  { title: "Mượn / Trả", url: "/borrowing", icon: ArrowLeftRight },
  { title: "Bán Sách", url: "/sales", icon: ShoppingCart },
  { title: "Thành viên", url: "/members", icon: Users },
  { title: "Thống kê", url: "/reports", icon: BarChart3 },
];

export function AppSidebar() {
  const { state } = useSidebar();
  const collapsed = state === "collapsed";

  return (
    <Sidebar collapsible="icon">
      <SidebarHeader className="border-b border-sidebar-border p-4">
        <div className="flex items-center gap-3">
          <Library className="h-7 w-7 text-sidebar-primary shrink-0" />
          {!collapsed && (
            <div>
              <h1 className="text-base font-bold text-sidebar-foreground leading-tight">
                Thư Viện
              </h1>
              <p className="text-xs text-sidebar-foreground/60">Trường THPT</p>
            </div>
          )}
        </div>
      </SidebarHeader>

      <SidebarContent className="pt-2">
        <SidebarGroup>
          <SidebarGroupContent>
            <SidebarMenu>
              {menuItems.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton asChild tooltip={item.title}>
                    <NavLink
                      to={item.url}
                      className="flex items-center gap-3 px-3 py-2 rounded-md text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground transition-colors"
                      activeClassName="bg-sidebar-accent text-sidebar-primary font-semibold"
                    >
                      <item.icon className="h-5 w-5 shrink-0" />
                      <span>{item.title}</span>
                    </NavLink>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
    </Sidebar>
  );
}
