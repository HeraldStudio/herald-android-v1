package cn.seu.herald_android.mod_query.library;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.seu.herald_android.R;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {

    private List<Book> bookList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView booksName;
        public TextView booksPosition;
        public TextView booksLeft;
        public TextView pubWithAuthor;

        public ViewHolder(View v) {
            super(v);
            booksName = (TextView)v.findViewById(R.id.books_name);
            booksPosition = (TextView)v.findViewById(R.id.books_position);
            booksLeft = (TextView)v.findViewById(R.id.books_left);
            pubWithAuthor = (TextView)v.findViewById(R.id.pub_author);
        }
    }

    public BookAdapter(List<Book> books) { this.bookList = books;}

    @Override
    public BookAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mod_que_library__search__item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.booksName.setText(book.name);
        holder.booksPosition.setText(book.index);
        holder.booksLeft.setText("剩余" + book.left + "本");
        holder.pubWithAuthor.setText(String.format("作者:%s\n出版社:%s" ,book.author, book.publish));
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public void setList(List<Book> bookses) {
        this.bookList = bookses;
    }
}
