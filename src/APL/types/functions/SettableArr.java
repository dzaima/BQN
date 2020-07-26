package APL.types.functions;

import APL.*;
import APL.errors.*;
import APL.types.*;

public class SettableArr extends Settable {
  public final Settable[] arr;
  public final int ia;
  public SettableArr(Settable[] arr) {
    ia = arr.length;
    if (arr.length > 0) this.token = arr[0].token;
    this.arr = arr;
  }
  
  public Arr get(Scope sc) {
    if (this.token != null) Main.faulty = this;
    Value[] res = new Value[arr.length];
    for (int i = 0; i < ia; i++) res[i] = arr[i].get(sc);
    return Arr.create(res);
  }
  
  
  public String toString() {
    return "vararr";
  }
  
  
  public void set(Value w, boolean update, Scope sc, Callable blame) {
    if (w.rank != 1) throw new LengthError((update?'↩':'←')+": scatter rank ≠1", w);
    if (w.ia != ia) throw new LengthError((update?'↩':'←')+": scatter argument lengths not equal", w);
    
    for (int i = 0; i < ia; i++) arr[i].set(w.get(i), update, sc, null);
  }
}