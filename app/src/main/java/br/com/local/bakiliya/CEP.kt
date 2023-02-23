package br.com.local.bakiliya

class CEP {
    private var cep: String? = null
    private var logradouro: String? = null
    private var complemento: String? = null
    private var bairro: String? = null
    private var localidade: String? = null
    private var uf: String? = null

    fun getCep(): String? { return cep }
    fun setCep(cep: String?) { this.cep = cep}

    fun getLogradouro(): String? { return logradouro }
    fun setLogradouro(logradouro: String?) { this.logradouro = logradouro}

    fun getComplemento(): String? { return complemento }
    fun setComplemento(complemento: String?) { this.complemento = complemento}

    fun getBairro(): String? { return bairro }
    fun setBairro(bairro: String?) { this.bairro = bairro}

    fun getLocalidade(): String? { return localidade }
    fun setLocalidade(localidade: String?) { this.localidade = localidade}

    fun getUf(): String? { return uf }
    fun setUf(uf: String?) { this.uf = uf}

    override fun toString(): String{
        var cepInfos = "CEP = ${getCep()}" +
                "\nLogradouro = ${getLogradouro()}" +
                "\nComplemento = ${getComplemento()}" +
                "\nBairro = ${getBairro()}" +
                "\nCidade = ${getLocalidade()}" +
                "\nEstado = ${getUf()}"
        cepInfos = cepInfos.trim()
        return cepInfos
    }
}