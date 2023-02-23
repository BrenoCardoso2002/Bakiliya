package br.com.local.bakiliya

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class MyAdapter(
    val contexto: Context,
    private val clienteArrayList: ArrayList<Cliente>,
    private val collectionName: String
): RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    // Cria a variavel do banco de dados:
    private lateinit var db: FirebaseFirestore

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        //inicializa o db:
        db = FirebaseFirestore.getInstance()

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_cliente, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val cliente: Cliente = clienteArrayList[position]
        Log.i("Firestore_Dados", cliente.toString())
        holder.nome.text = cliente.Nome
        holder.codigo.text = cliente.Codigo
        holder.cep.text = cliente.CEP
        if (cliente.Tipo == "Pessoa"){
            holder.ttNome.text = "Nome:"
            holder.ttcodigo.text = "CPF:"
            holder.imgTipo.setImageResource(R.drawable.ic_baseline_person_24)
        }else if(cliente.Tipo == "Empresa"){
            holder.ttNome.text = "Nome fantasia:"
            holder.ttcodigo.text = "CNPJ:"
            holder.imgTipo.setImageResource(R.drawable.ic_baseline_factory_24)
        }
        holder.bDelete.setOnClickListener{
            /*
            if (cliente.Tipo == "Pessoa"){
                Toast.makeText(contexto, "Clicou para apagar cliente pessoa!!!", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(contexto, "Clicou para apagar cliente empresa!!!", Toast.LENGTH_LONG).show()
            }
             */
            val query = db.collection(collectionName).whereEqualTo("Codigo", cliente.Codigo)
            query.get().addOnSuccessListener { documents ->
                for (document in documents){
                    document.reference.delete()
                }
                holder.layout.setBackgroundResource(R.drawable.ic_baseline_block_24)
                cliente.Tipo = "Apagado"
            }
                .addOnFailureListener {
                    Toast.makeText(contexto, "Não foi possivel apagar o cliente", Toast.LENGTH_LONG).show()
                }
        }
        holder.itemView.setOnClickListener{
            if (cliente.Tipo == "Pessoa"){
                val intent = Intent(contexto, View_Cliente_Pessoa::class.java)

                intent.putExtra("Nome", cliente.Nome.toString())
                intent.putExtra("Codigo", cliente.Codigo.toString())
                intent.putExtra("Email", cliente.Email.toString())
                intent.putExtra("Contato", cliente.Contato.toString())
                intent.putExtra("Cep", cliente.CEP.toString())
                intent.putExtra("NRua", cliente.NRua.toString())

                contexto.startActivity(intent)
            }else if(cliente.Tipo == "Empresa"){
                val intent = Intent(contexto, View_Cliente_Empresa::class.java)

                intent.putExtra("Nome", cliente.Nome.toString())
                intent.putExtra("Codigo", cliente.Codigo.toString())
                intent.putExtra("Email", cliente.Email.toString())
                intent.putExtra("Contato", cliente.Contato.toString())
                intent.putExtra("Cep", cliente.CEP.toString())
                intent.putExtra("NRua", cliente.NRua.toString())

                contexto.startActivity(intent)
            }else{
                Toast.makeText(contexto, R.string.cliente_ja_foi_apagado_ou_seja_n_o_possivel_visualizar_os_seus_dados, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun getItemCount(): Int {
        val Count = clienteArrayList.size
        return Count
    }


    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        // variaveis do item empresa:
        val nome: TextView = itemView.findViewById(R.id.TextView_Nome_ItemCliente)
        val codigo: TextView = itemView.findViewById(R.id.TextView_CODIGO_ItemCliente)
        val cep: TextView = itemView.findViewById(R.id.TextView_CEP_ItemCliente)
        val ttNome: TextView = itemView.findViewById(R.id.tituloNome_ItemCliente)
        val ttcodigo: TextView = itemView.findViewById(R.id.tituloCodigo_ItemCliente)
        val imgTipo: ImageView = itemView.findViewById(R.id.imgTipoCliente_ItemCliente)
        val bDelete: ImageButton = itemView.findViewById(R.id.Button_Delete_ItemCliente)
        val layout: LinearLayout = itemView.findViewById(R.id.LinearLayoutItem)
    }
}
