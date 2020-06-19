package APL.types.functions;

import APL.*;
import APL.errors.LengthError;
import APL.types.*;
import APL.types.functions.builtins.SetBuiltin;

import java.util.Arrays;

public class SettableArr extends Settable {
  public final Settable[] arr;
  public final int ia;
  public SettableArr(Settable[] arr) {
    super(null);
    ia = arr.length;
    if (arr.length > 0) this.token = arr[0].token;
    this.arr = arr;
  }
  
  public Arr get() {
    if (this.token != null) Main.faulty = this;
    Value[] res = new Value[arr.length];
    for (int i = 0; i < ia; i++) res[i] = arr[i].get();
    return Arr.create(res);
  }
  
  @Override
  public Type type() {
    return Type.array;
  }
  
  
  @Override
  public String toString() {
    if (Main.debug) return "vararr:"+ Arrays.toString(arr);
    return get().toString();
  }
  
  
  public void set(Value w, boolean update, Callable blame) {
    if (w.rank != 1) throw new LengthError((update?'↩':'←')+": scatter rank ≠1", w);
    if (w.ia != ia) throw new LengthError((update?'↩':'←')+": scatter argument lengths not equal", w);
    for (int i = 0; i < ia; i++) {
      SetBuiltin.set(arr[i], w.get(i), update);
    }
  }
}