import { useState, useRef, useEffect } from "react";
import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { books, members } from "@/data/mockData";
import { useNavigate } from "react-router-dom";

export function GlobalSearch() {
  const [query, setQuery] = useState("");
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  const q = query.toLowerCase();
  const matchedBooks = q ? books.filter((b) => b.title.toLowerCase().includes(q) || b.author.toLowerCase().includes(q)).slice(0, 5) : [];
  const matchedMembers = q ? members.filter((m) => m.name.toLowerCase().includes(q)).slice(0, 3) : [];
  const hasResults = matchedBooks.length > 0 || matchedMembers.length > 0;

  return (
    <div ref={ref} className="relative hidden md:block">
      <div className="relative">
        <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Tìm sách, thành viên..."
          value={query}
          onChange={(e) => { setQuery(e.target.value); setOpen(true); }}
          onFocus={() => query && setOpen(true)}
          className="pl-8 w-64 h-9 bg-muted/50"
        />
      </div>
      {open && query && (
        <div className="absolute top-full mt-1 w-80 bg-popover border border-border rounded-lg shadow-lg z-50 overflow-hidden">
          {!hasResults && <p className="p-3 text-sm text-muted-foreground">Không tìm thấy kết quả</p>}
          {matchedBooks.length > 0 && (
            <div>
              <p className="px-3 pt-2 pb-1 text-xs font-semibold text-muted-foreground uppercase">Sách</p>
              {matchedBooks.map((b) => (
                <button key={b.id} className="w-full text-left px-3 py-2 hover:bg-accent/10 transition-colors flex justify-between items-center" onClick={() => { navigate(`/books/${b.id}`); setOpen(false); setQuery(""); }}>
                  <div>
                    <p className="text-sm font-medium">{b.title}</p>
                    <p className="text-xs text-muted-foreground">{b.author}</p>
                  </div>
                </button>
              ))}
            </div>
          )}
          {matchedMembers.length > 0 && (
            <div>
              <p className="px-3 pt-2 pb-1 text-xs font-semibold text-muted-foreground uppercase">Thành viên</p>
              {matchedMembers.map((m) => (
                <button key={m.id} className="w-full text-left px-3 py-2 hover:bg-accent/10 transition-colors" onClick={() => { navigate("/members"); setOpen(false); setQuery(""); }}>
                  <p className="text-sm font-medium">{m.name}</p>
                  <p className="text-xs text-muted-foreground">Lớp {m.class}</p>
                </button>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
