package com.tinkerpop.gremlin;

import org.apache.commons.jxpath.ClassFunctions;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import com.tinkerpop.gremlin.db.tg.TinkerFunctions;
import com.tinkerpop.gremlin.db.sesame.SesameFunctions;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version 0.1
 */
public class GremlinPathContext extends JXPathContextReferenceImpl {

    //protected String contextPath;

    protected Graph graph;
    private boolean newRoot = false;

    static {
        JXPathIntrospector.registerDynamicClass(Vertex.class, VertexPropertyHandler.class);
        JXPathIntrospector.registerDynamicClass(Edge.class, EdgePropertyHandler.class);
    }

    public GremlinPathContext(JXPathContext parentContext, Object element) {
        super(parentContext, element);
        if (null == parentContext) {
            FunctionLibrary library = new FunctionLibrary();
            library.addFunctions(new ClassFunctions(GremlinFunctions.class, GremlinFunctions.NAMESPACE_PREFIX));
            library.addFunctions(new ClassFunctions(TinkerFunctions.class, TinkerFunctions.NAMESPACE_PREFIX));
            library.addFunctions(new ClassFunctions(SesameFunctions.class, SesameFunctions.NAMESPACE_PREFIX));
            library.addFunctions(this.getFunctions());
            this.setFunctions(library);
            this.getVariables().declareVariable("_", null);
        }
    }

    public GremlinPathContext(Object element) {
        this(null, element);
    }

    public static JXPathContext newContext(JXPathContext parentContext, Object element) {
        return new GremlinPathContext(parentContext, element);
    }

    public static JXPathContext newContext(Object element) {
        return GremlinPathContext.newContext(null, element);
    }

    /* public List selectNodes(String path) {
        //System.out.println(this.contextBean + " set_root nodes");
        //this.contextPath = path;
        return super.selectNodes(path);
    }*/

    /*public Iterator iterate(String path) {
        //this.contextPath = path;
        return super.iterate(path);
    }*/

    /*public String getContextPath() {
        return this.contextPath;
    }*/

    public void setContextBean(Object root) {
        this.contextBean = root;
        this.newRoot = true;
    }

    public boolean rootChanged() {
        return this.newRoot;
    }
}
