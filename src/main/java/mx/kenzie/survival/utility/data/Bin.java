package mx.kenzie.survival.utility.data;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class Bin {

    protected final Set<Reference<?>> references = new HashSet<>();
    protected final Map<Object, Collection<Reference<Object>>> data = new HashMap<>();
    protected final Function<Object, Reference<Object>> factory;

    protected Bin(Function<Object, Reference<Object>> factory) {
        this.factory = factory;
    }

    public Bin() {
        this(SoftReference::new);
    }

    public static Bin bin() {
        return new Bin();
    }

    public void bin(Object value, Object... context) {
        Reference<Object> reference = factory.apply(value);
        references.add(reference);
        for (Object o : context) {
            data.computeIfAbsent(o, _ -> new HashSet<>()).add(reference);
        }
    }

    public <For> Rummage<For> rummage(Class<For> type, Object... context) {
        List<Reference<?>> list = new ArrayList<>(references);

        for (Object o : context) {
            Collection<Reference<Object>> orDefault = data.getOrDefault(o, Collections.emptySet());
            list.retainAll(orDefault);
        }
        return new Rummage<>(type, list);

    }

    public Rummage<Object> rummage(Object... context) {
        return this.rummage(Object.class, context);
    }


    public static class Rummage<For> {

        final List<Reference<?>> list;
        final Class<For> type;
        List<For> values;

        public Rummage(Class<For> type, List<Reference<?>> list) {
            this.type = type;
            this.list = list;
        }

        public boolean found() {
            return !this.look().isEmpty();
        }

        public For find() {
            List<For> look = this.look();
            return look.getFirst();
        }

        public Iterable<For> findAll() {
            try {
                return values;
            } finally {
                this.values = null;
            }
        }

        protected List<For> look() {
            if (values != null) return values;
            values = new ArrayList<>();
            for (Reference<?> reference : list) {
                Object o = reference.get();
                if (type.isInstance(o)) values.add(type.cast(o));
            }
            return values;
        }

        public For find(Supplier<For> otherwise) {
            if (this.found()) return this.find();
            return otherwise.get();
        }

    }

}
