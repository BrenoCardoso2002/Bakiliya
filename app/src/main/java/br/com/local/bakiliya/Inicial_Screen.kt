package br.com.local.bakiliya

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class Inicial_Screen : AppCompatActivity() {
    // Cria variavel do firebaseAuth:
    private lateinit var auth: FirebaseAuth

    // Cria a variavel do banco de dados:
    private lateinit var db: FirebaseFirestore

    // variaveis para o firestore:
    private lateinit var uid: String
    private lateinit var collectionName: String
    private lateinit var tipo: String

    // Criar variavel dos campos da tela:
    private lateinit var txtNomeUser: TextView
    private lateinit var bConfig: ImageButton
    private lateinit var bCEmpresa: Button
    private lateinit var bCPessoa: Button

    // cria variaveis para o recycler view:
    private lateinit var recyclerView: RecyclerView
    private lateinit var tarefaArrayList: ArrayList<Cliente>
    private lateinit var myAdapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicial_screen)
        if(!verificaInternet()){
            alertaSemConexao()
        }

        // Inicializa a variavel do firebaseAuth:
        auth = Firebase.auth

        //inicializa o db:
        db = FirebaseFirestore.getInstance()

        // obtem o uid do usuário
        uid = auth.currentUser?.uid.toString()

        // obtem o nome da coleão que tera os clientes:
        collectionName = "${uid}_clientes"
        Log.i("CollectionName", collectionName)

        // Inicializa as variaveis dos campos:
        txtNomeUser = findViewById(R.id.TextView_NomeUserConnected)
        bConfig = findViewById(R.id.ImageButton_Configuracao_InicialScreen)
        bCEmpresa = findViewById(R.id.Button_cEmpresa_InicialScreen)
        bCPessoa = findViewById(R.id.Button_cPessoa_InicialScreen)
        recyclerView = findViewById(R.id.recycerViewClientes)

        colocaNome()

        bConfig.setOnClickListener{
            if(!verificaInternet()){
                alertaSemConexao()
            }
            @SuppressLint("InflateParams")
            val dialogBinding = layoutInflater.inflate(R.layout.configuracao_alert, null)
            val myDialog = Dialog(this)
            myDialog.setContentView(dialogBinding)
            myDialog.setCancelable(true)
            myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            myDialog.show()
            myDialog.findViewById<Button>(R.id.Button_logout_InicialScreen).setOnClickListener {
                myDialog.dismiss()
                auth.signOut()
                startActivity(Intent(this, LogIn::class.java))
                finish()
            }
            myDialog.findViewById<Button>(R.id.Button_Fechar_InicialScreen).setOnClickListener {
                myDialog.dismiss()
                finish()
            }
            myDialog.findViewById<Button>(R.id.Button_Deletar_InicialScreen).setOnClickListener {
                myDialog.dismiss()
                val user = auth.currentUser
                user?.delete()
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val collectionRef = db.collection(collectionName)
                            collectionRef.get().addOnSuccessListener { documents ->
                                // Exclua cada documento encontrado
                                for (document in documents) {
                                    document.reference.delete()
                                }
                            }
                            //verifiar o tipo do usuario e apagar tbm
                            val collectionRef2 = db.collection("UsuarioPessoa").whereEqualTo("UID", uid)
                            collectionRef2.get().addOnSuccessListener { documents ->
                                // Exclua cada documento encontrado
                                for (document in documents) {
                                    document.reference.delete()
                                }
                            }
                            auth.signOut()
                            startActivity(Intent(this, LogIn::class.java))
                            finish()
                        } else {
                            val dialogBinding2 = layoutInflater.inflate(R.layout.error_delete_user, null)
                            val myDialog2 = Dialog(this)
                            myDialog2.setContentView(dialogBinding2)
                            myDialog2.setCancelable(true)
                            myDialog2.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                            myDialog2.show()
                            myDialog2.findViewById<Button>(R.id.Button_OK_erroDeleteUser).setOnClickListener {
                                myDialog.dismiss()
                            }
                        }
                    }

            }
        }

        bCEmpresa.setOnClickListener{
            if(!verificaInternet()){
                alertaSemConexao()
            }
            startActivity(Intent(this, Add_Cliente_Empresa::class.java))
            finish()
        }

        bCPessoa.setOnClickListener{
            if(!verificaInternet()){
                alertaSemConexao()
            }
            startActivity(Intent(this, Add_Cliente_Pessoa::class.java))
            finish()
        }

        tarefaArrayList = arrayListOf()
        myAdapter = MyAdapter(this, tarefaArrayList, collectionName)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = myAdapter

        // recyclerview programming:
        loadRecyclerView()
    }

    fun loadRecyclerView() {
        if(!verificaInternet()){
            alertaSemConexao()
        }
        val collectionRef = db.collection(collectionName)

        collectionRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    //Log.i("Firestore_Dados", "${document.id} => ${document.data}")
                    //Log.i("Firestore_Dados", "${document["Tipo"]}")
                    val adiciona = document.toObject(Cliente::class.java)
                    Log.i("Firestore_Dados", "$adiciona")
                        tarefaArrayList.add(adiciona)
                }
                myAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.i("Firestore_Dados", "Error getting documents: ", exception)
            }
    }

    private fun colocaNome() {
        if(!verificaInternet()){
            alertaSemConexao()
        }
        // txtNomeUser.text = uid

        val usuariosRef = db.collection("UsuarioPessoa")
        val consulta = usuariosRef.whereEqualTo("UID", uid)

        consulta.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val dadosUser = document.data
                    val nomeUser = dadosUser["Nome"].toString()
                    val tipoUser = dadosUser["Tipo"].toString()
                    txtNomeUser.text = "$nomeUser, seja bem vindo!"
                    tipo = tipoUser
                    //Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener {
                val usuariosRef2 = db.collection("UsuarioEmpresa")
                val consulta2 = usuariosRef2.whereEqualTo("UID", uid)

                consulta2.get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            val dadosUser = document.data
                            val nomeUser = dadosUser["Nome"].toString()
                            val tipoUser = dadosUser["Tipo"].toString()
                            txtNomeUser.text = "$nomeUser, seja bem vindo!"
                            tipo = tipoUser
                            //Log.d(TAG, "${document.id} => ${document.data}")
                        }
                    }
                    .addOnFailureListener { exception ->
                        txtNomeUser.text = "Erro ao buscar nome!"
                        //Log.w(TAG, "Erro ao executar consulta", exception)
                    }
            }
    }

    private fun verificaInternet(): Boolean {
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        return isConnected
    }

    private fun alertaSemConexao() {
        val layout = R.layout.sem_conexao
        val dialogBinding = layoutInflater.inflate(layout, null)
        val myDialog = Dialog(this)
        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(false)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.show()
        myDialog.findViewById<Button>(R.id.Btn_Reconectar).setOnClickListener {
            if(verificaInternet()){
                myDialog.dismiss()
            }else{
                Toast.makeText(this, "Ainda não há conexão!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
