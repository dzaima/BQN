package APL.types.dimensions;

import APL.Scope;
import APL.errors.SyntaxError;
import APL.types.Value;
import APL.types.functions.*;

public class DervDimMop extends Mop {
  private final Mop f;
  private final int dim;
  
  public DervDimMop(Mop f, Integer dim, Scope sc) {
    super(sc);
    this.f = f;
    if (dim == null) this.dim = 0;
    else if (dim < 0) this.dim = dim;
    else this.dim = dim;
    this.token = f.token;
    
  }
  
  @Override public Value call(Value aa, Value a, Value w, DerivedMop derv) {
    if (!(f instanceof DimDMop)) throw new SyntaxError("Attempt to call function dyadically that doesn't support dimension specification", a);
    return ((DimDMop) f).call(aa, a, w, dim);
  }
  
  @Override public Value call(Value aa, Value w, DerivedMop derv) {
    if (!(f instanceof DimMMop)) throw new SyntaxError("Attempt to call function monadically that doesn't support dimension specification", w);
    return ((DimMMop) f).call(aa, w, dim);
  }
  
  @Override public String repr() {
    return f.repr()+"["+dim+"]";
  }
}