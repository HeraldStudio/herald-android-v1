package cn.seu.herald_android.mod_query.library;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;

public class SearchBookAdapter extends RecyclerView.Adapter<SearchBookAdapter.ViewHolder> {

    private List<SearchBookModel> bookModelList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.books_name)
        TextView booksName;
        @BindView(R.id.books_position)
        TextView booksPosition;
        @BindView(R.id.books_left)
        TextView booksLeft;
        @BindView(R.id.pub_author)
        TextView pubWithAuthor;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
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

    public void setList(List<SearchBookModel> books) {
        this.bookModelList = books;
    }
}
