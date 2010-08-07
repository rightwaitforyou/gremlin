package com.tinkerpop.gremlin.compiler.types;

import com.tinkerpop.gremlin.compiler.Tokens;
import com.tinkerpop.gremlin.compiler.context.GremlinScriptContext;
import com.tinkerpop.gremlin.compiler.context.VariableLibrary;
import com.tinkerpop.gremlin.compiler.pipes.GremlinRangeFilterPipe;
import com.tinkerpop.pipes.MultiIterator;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.Pipeline;
import com.tinkerpop.pipes.SingleIterator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Pavel A. Yaskevich
 */
final public class GPath extends DynamicEntity implements Iterable {

    private final List<Pipe> pipes;
    private final Atom<Object> root;
    private Object persistentRoot = null;
    private Pipeline pipeline;
    private final Set<Object> previouslyFetched;

    public GPath(final Atom<Object> root, final List<Pipe> pipes, final GremlinScriptContext context) {
        this.root  = root;
        this.pipes = pipes;
        this.previouslyFetched = new HashSet<Object>();
        
        if (!(root instanceof DynamicEntity)) {
            if (root.toString().equals(".") && root.isIdentifier()) {
                final VariableLibrary variables = context.getVariableLibrary();
                this.persistentRoot = ((Atom) variables.get(Tokens.ROOT_VARIABLE)).getValue();
            }
        }
    }

    protected Object value() {
        Object top;
        Iterator pipeline = this.iterator();
        
        if(pipeline.hasNext())
            top = pipeline.next();
        else
            return null;

        if (pipeline.hasNext()) {
            this.previouslyFetched.add(top);
            return this;
        } else return top;
    }
    
    public Iterator iterator() {
        for(Pipe p : this.pipes) {
            if (p instanceof GremlinRangeFilterPipe) {
                ((GremlinRangeFilterPipe) p).reset();
            }
        }

        if (this.pipeline == null || !this.pipeline.hasNext()) {
            this.pipeline = new Pipeline(this.pipes);
            this.pipeline.setStarts(this.pipelineRoot());
        } else if (this.pipeline.hasNext()) { 
            return new MultiIterator<Object>(this.previouslyFetched.iterator(), pipeline);
        }
        
        return this.pipeline;
    }
    
    private Iterator pipelineRoot() {
        final Object root = (persistentRoot == null) ? this.root.getValue() : this.persistentRoot;
        
        if (root instanceof Iterable) {
            return ((Iterable) root).iterator();
        } else if (root instanceof Iterator) {
            return (Iterator) root;
        } else {
            return new SingleIterator<Object>(root);
        }
    }

    public boolean equals(Object o) {
        if (o instanceof Iterable) {
            Iterator itty = ((Iterable) o).iterator();

            for (Object element : this) {
                if (itty.hasNext()) {
                    if (!element.equals(itty.next()))
                        return false;
                } else {
                    return false;
                }
            }

            // if there are still elements left in the comparable object
            // when gpath is out of the elements return false
            if (itty.hasNext()) return false;
        } else
            return super.equals(o);
        
        return true;
    }

    public String toString() {
        String result = "";
        
        for (Object o : this)
            result += o.toString() + ", ";

        return "[" + result.substring(0, result.length() - 2) + "]";
    }
}