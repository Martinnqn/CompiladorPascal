program factorial;
var
    a: integer;
function fact(n:integer):integer;
begin
    if n < 2 then fact := 1
    else fact := fact(n-1)*n
end;
Begin
    read (a);
    write (fact(a))
End.