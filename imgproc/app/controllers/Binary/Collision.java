package controllers.Binary;

// collision class to represent a collision of two label values
public class Collision {
	public int a, b;

	public Collision(int label_a, int label_b) {
		a = label_a;
		b = label_b;
	}

    public boolean equals(Object obj) {
            if (obj instanceof Collision) {
                    Collision c = (Collision) obj;
                    return (this.a == c.a && this.b == c.b);
            } else
                    return false;
    }
}