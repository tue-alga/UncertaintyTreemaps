package com.eduardovernier;

public class Entity {
    String id;
    Double weight;

    public Entity(String id, Double weight) {
        this.id = id;
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entity entity = (Entity) o;

        return id != null ? id.equals(entity.id) : entity.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id='" + id + '\'' +
                ", weight=" + weight +
                '}';
    }
}
