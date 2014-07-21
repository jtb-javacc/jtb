package EDU.purdue.jtb.misc;

import static EDU.purdue.jtb.misc.Globals.LS;


@SuppressWarnings("javadoc")
public class NodeTokenClass extends AbstractNodeClass {

  @Override
  public String getName() {
    return "NodeToken";
  }

  @Override
  public void genFileHeader(StringBuilder sb) {
    sb.append("#include ").append("<string>").append(LS);
    sb.append("#include ").append("<vector>").append(LS);
    sb.append("#include ").append('"').append("INode.h").append('"').append(LS);
    sb.append("#include ").append('"').append("IRetArguVisitor.h").append('"').append(LS);
    sb.append("#include ").append('"').append("NoSuchElementException.h").append('"').append(LS);
    sb.append("using namespace std;").append(LS);
  }

}