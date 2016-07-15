package cn.seu.herald_android.mod_query.library;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.seu.herald_android.R;

public class SearchBookAdapter extends RecyclerView.Adapter<SearchBookAdapter.ViewHolder> {

    private List<SearchBookModel> bookModelList;

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

    public SearchBookAdapter(List<SearchBookModel> bookModels) {
        this.bookModelList = bookModels;
    }

    @Override
    public SearchBookAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mod_que_library__search__item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SearchBookModel bookModel = bookModelList.get(position);
        holder.booksName.setText(bookModel.name);
        holder.booksPosition.setText(bookModel.index);
        holder.booksLeft.setText("剩余" + bookModel.left + "本");
        holder.pubWithAuthor.setText(String.format("作者:%s\n出版社:%s", bookModel.author, bookModel.publish));
    }

    @Override
    public int getItemCount() {
        return bookModelList.size();
    }

    public void setList(List<SearchBookModel> bookses) {
        this.bookModelList = bookses;
    }
}
