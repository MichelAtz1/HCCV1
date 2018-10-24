package br.desenvolvedor.michelatz.aplicativohcc.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import br.desenvolvedor.michelatz.aplicativohcc.Modelo.DadosGerais;
import br.desenvolvedor.michelatz.aplicativohcc.R;

public class AdapterListViewPostes extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<DadosGerais> itens;

    public static String idSelecionado;

    public AdapterListViewPostes(Context context, ArrayList<DadosGerais> itens) {
        this.itens = itens;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return itens.size();
    }

    public DadosGerais getItem(int position) {
        return itens.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View view, ViewGroup parent) {
        DadosGerais item = itens.get(position);
        view = mInflater.inflate(R.layout.list_item_poste, null);
        ((TextView) view.findViewById(R.id.txtMensagem)).setText(item.getTexto());
        //((ImageButton) view.findViewById(R.id.btnDelete5)).setTag(position);
        ((ImageButton) view.findViewById(R.id.btnEdit)).setTag(position);
        return view;
    }

    public void editaItem(int positionToEdit) {
        DadosGerais item = itens.get(positionToEdit);
        idSelecionado = item.getId();
        notifyDataSetChanged();
    }

    public void excluiItem(int positionToRemove) {
        DadosGerais item = itens.get(positionToRemove);
        idSelecionado = item.getId();
        notifyDataSetChanged();
    }

}
