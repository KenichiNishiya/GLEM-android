package com.example.glem

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import com.google.firebase.firestore.FirebaseFirestore


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var text_show: TextView
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        // Inicializando elementos View
        val marca = rootView.findViewById<EditText>(R.id.inputMarca)
        val modelo = rootView.findViewById<EditText>(R.id.inputModelo)
        val ano = rootView.findViewById<EditText>(R.id.inputAno)
        val imageView = rootView.findViewById<ImageView>(R.id.imageView)

        text_show = rootView.findViewById<TextView>(R.id.show_text)

        // Criando a conexao com o bd do Firebase
        val storage = Firebase.storage("gs://glem-android.appspot.com")
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/AE86.jpg")
        val localFile = File.createTempFile("tempImage", "jpg")

        imageRef.getFile(localFile)
            .addOnSuccessListener {
                // Se conseguir baixar a imagem, transforma em bitmap, dimensiona e seta na view
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 700, 500, true)
                imageView.setImageBitmap(scaledBitmap)
            }
            .addOnFailureListener {
                // Executa se acontecer algum erro
                Toast.makeText(activity, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show()
            }

        val add_button: Button = rootView.findViewById(R.id.button_add)
        add_button.setOnClickListener {
            // Pega o input do user
            val marcaText = marca.text.toString()
            val modeloText = modelo.text.toString()
            val anoText = ano.text.toString()

            // Insere no bd se os campos estiverem preenchidos
            if(marcaText.isNotBlank() && modeloText.isNotBlank() && anoText.isNotBlank()) {
                addDataToFirestore(marcaText, modeloText, anoText)

                marca.setText("")
                modelo.setText("")
                ano.setText("")
            }
            else{
                Toast.makeText(activity, "Digite todos os dados", Toast.LENGTH_LONG).show()
            }
        }

        val show_button: Button = rootView.findViewById(R.id.button_show)
        show_button.setOnClickListener {
            retrieveDataFromFirestore()
        }

        val buttonFragment: Button = rootView.findViewById(R.id.button_fragment)

        buttonFragment.setOnClickListener {
            // Create a FragmentManager
            val fragmentManager: FragmentManager = requireActivity().supportFragmentManager

            // Begin a FragmentTransaction
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()

            // Replace the contents of the container with the new fragment
            val fragment = ScrollingFragment()
            fragmentTransaction.replace(R.id.fragment_container, fragment)

            // Add the transaction to the back stack
            fragmentTransaction.addToBackStack(null)

            // Commit the transaction
            fragmentTransaction.commit()
        }

        return rootView
    }

    fun addDataToFirestore(marca: String, modelo: String, ano: String) {

        // Cria uma referencia ao Firestore
        val db = FirebaseFirestore.getInstance()

        // Cria um novo documento na collection de carros
        val carrosCollection = db.collection("carros")
        val newCarDoc = carrosCollection.document()

        // Adiciona dados no documento
        val data = hashMapOf(
            "marca" to marca,
            "modelo" to modelo,
            "ano" to ano
        )

        newCarDoc.set(data)
            .addOnSuccessListener {
                Toast.makeText(activity, "Inserido com sucesso", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity, "Erro ao inserir o documento", Toast.LENGTH_SHORT).show()
            }
    }

    fun retrieveDataFromFirestore() {

        // Cria uma referecia ao Firestore > collection carros
        val db = FirebaseFirestore.getInstance()
        val carrosCollection = db.collection("carros")

        // Faz um get para pegar todos os dados da collection
        carrosCollection.get()
            .addOnSuccessListener { result ->
                // Cria uma string pra colocar todos os itens
                val dataStringBuilder = StringBuilder()

                // Loop para iterar cada elemento da collection
                for (document in result) {
                    val marca = document.getString("marca")
                    val modelo = document.getString("modelo")
                    val ano = document.getString("ano")

                    dataStringBuilder.append("Marca: $marca, Modelo: $modelo, Ano: $ano\n")
                }

                // Atualiza a textview
                val allData = dataStringBuilder.toString()
                text_show.text = allData
            }
            .addOnFailureListener { exception ->
                Toast.makeText(activity, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
            }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}