import java.util.LinkedList;

public class Queue<E> {
    LinkedList<E> list;
    public Queue(){
        list = new LinkedList<>();
    }
    public void add(E e){
        list.add(e);
    }
    public E getNext(){
        return list.poll();
    }
    public E peakNext(){
        return list.peek();
    }
    public int size(){
        return list.size();
    }
    public boolean isEmpty(){
        return list.isEmpty();
    }
}
