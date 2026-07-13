package mx.kenzie.survival.utility.pack;

public class Item extends Data {

    public Model model = new Model();

    public Item() {

    }

    public Item(String modelPath) {
        this();
        this.model.model = modelPath;
    }

    public class Model {
        public String type = "minecraft:model";
        public String model;
    }

}
