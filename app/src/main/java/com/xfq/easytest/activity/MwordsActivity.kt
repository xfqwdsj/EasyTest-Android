package com.xfq.easytest.activity

import androidx.appcompat.app.AppCompatActivity

class MwordsActivity : AppCompatActivity() {
    /*
    private lateinit var binding: ActivityMwordsBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var customHelp by Delegates.notNull<Int>()
    private var index = 0
    private var time = 0
    private var help = 0
    private var timer = false
    private lateinit var job: Job
    private lateinit var wordsList: MutableList<Word>
    private lateinit var urlList: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMwordsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        customHelp = sharedPreferences.getString("custom_help", "0")!!.toInt()
        getList()
        initViews()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return false
    }

    private fun getList() {
        val dialog = BottomDialog().create(this@MwordsActivity).apply {
            title = "${getResString(R.string.loading_sources)} 1/2"
            setContent(R.string.please_wait)
            setCancelAble(false)
            show()
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://xfqwdsj.github.io/mword/words.json")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                dialog.close()
                error(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val data = response.body.string()
                dialog.close()
                if (data != null) {
                    val json = JSON.parseArray(data, MyUnit::class.java)
                    val unitsArray = Array(json.size) { "" }
                    urlList = MutableList(json.size) { "" }
                    for (i in 0 until json.size) {
                        unitsArray[i] = json[i].displayName
                        urlList[i] = json[i].url
                    }
                    val adapter = ArrayAdapter(
                        this@MwordsActivity,
                        android.R.layout.simple_spinner_item,
                        unitsArray
                    ).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                    runOnUiThread {
                        binding.spinner.adapter = adapter
                        binding.spinner.onItemSelectedListener = object : OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                i: Int,
                                id: Long
                            ) {
                                init(urlList[i])
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }
                    }
                } else {
                    finish()
                }
            }
        })
    }

    private fun init(url: String?) {
        stopTimer()
        help = 0
        findViewById<TextView>(R.id.help).text = help.toString()
        val dialog = BottomDialog().create(this@MwordsActivity).apply {
            title = "${getResString(R.string.loading_sources)} 2/2"
            setContent(R.string.please_wait)
            setCancelAble(false)
            show()
        }
        var words: String?
        if (url != null) {
            Thread(Runnable {
                try {
                    val client = OkHttpClient()
                    val request = Request.Builder()
                        .url(url)
                        .build()
                    val response = client.newCall(request).execute()
                    words = response.body.string()
                    if (words != null) {
                        index = 0
                        parseWordsXml(words!!)
                        randomSort(wordsList)
                        val wordsTextView = findViewById<TextView>(R.id.words)
                        runOnUiThread {
                            wordsTextView.text = wordsList.toString()
                            binding.progress.text = 0.toString()
                            binding.all.text = wordsList.size.toString()
                            binding.progressBar.max = wordsList.size
                            binding.progressBar.progress = 0
                            startWrite(wordsList)
                            dialog.close()
                        }
                        startTimer()
                    } else {
                        dialog.close()
                        finish()
                    }
                } catch (e: Exception) {
                    dialog.close()
                    error(e)
                }
            }).start()
        }
    }

    private fun startTimer() {
        timer = true
        time = -1
        val textView = findViewById<TextView>(R.id.timer)
        textView.text = (-1).toString()
        binding.timerStatus.text = timer.toString()
        if (this::job.isInitialized && job.isActive) job.cancel()
        job = GlobalScope.launch(Dispatchers.Main) {
            while (timer) {
                time++
                textView.text = (textView.text.toString().toInt() + 1).toString()
                if (textView.text.toString()
                        .toInt() != time && binding.timerStatus.text.toString() != timer.toString()
                ) {
                    finish()
                }
                delay(1000)
            }
        }
    }

    private fun stopTimer() {
        if (this::job.isInitialized && job.isActive) job.cancel()
        val textView = findViewById<TextView>(R.id.timer)
        if (textView.text.toString()
                .toInt() != time && binding.timerStatus.text.toString() != timer.toString()
        ) {
            finish()
        }
        timer = false
        binding.timerStatus.text = timer.toString()
        binding.editText.setText("")
        binding.editText.hint = ""
    }

    private fun stopTimerGetTime(): String? {
        return if (index + 1 == binding.all.text.toString()
                .toInt() && index + 1 == binding.progress.text.toString().toInt()
        ) {
            stopTimer()
            val returnTime: String
            val timeMinutesString: String
            val timeSecondsString: String
            var timeMinutes = 0
            var timeSeconds = time
            while (timeSeconds >= 60) {
                timeSeconds -= 60
                timeMinutes++
            }
            timeSecondsString = if (timeSeconds < 10) {
                "0$timeSeconds"
            } else {
                timeSeconds.toString()
            }
            timeMinutesString = timeMinutes.toString()
            returnTime = "$timeMinutesString:$timeSecondsString"
            returnTime
        } else {
            finish()
            null
        }
    }

    private fun startWrite(words: MutableList<Word>) {
        binding.textTop.text = words[index].trans
        binding.editText.isEnabled = true
        binding.button1.isEnabled = true
        binding.button2.isEnabled = true
    }

    private fun next() {
        if (binding.editText.text.toString() == wordsList[index].word) {
            binding.editText.setText("")
            binding.progressBar.progress = binding.progressBar.progress + 1
            binding.progress.text = (binding.progress.text.toString().toInt() + 1).toString()
            if (index + 1 == binding.all.text.toString()
                    .toInt() && index + 1 == binding.progress.text.toString().toInt()
            ) {
                if (job.isActive) {
                    GlobalScope.launch(Dispatchers.Main) {
                        binding.textTop.setText(R.string.congratulations)
                        BottomDialog().create(this@MwordsActivity).apply {
                            setTitle(R.string.congratulations)
                            setContent(
                                this@MwordsActivity.resources.getString(
                                    R.string.mwords_result,
                                    stopTimerGetTime()
                                )
                            )
                            setButton1(android.R.string.ok) {
                                close()
                                upload()
                            }
                            setButton2(android.R.string.cancel) {
                                close()
                            }
                            setCancelAble(false)
                            show()
                        }
                        binding.editText.hint = ""
                        binding.editText.isEnabled = false
                        binding.button1.isEnabled = false
                        binding.button2.isEnabled = false
                    }
                } else {
                    finish()
                }
            } else if (index + 1 < binding.all.text.toString()
                    .toInt() && index + 1 == binding.progress.text.toString().toInt()
            ) {
                index++
                binding.textTop.text = wordsList[index].trans
            } else {
                finish()
            }
        } else {
            binding.editText.setText("")
            binding.editText.setHint(R.string.incorrect_write)
        }
    }

    private fun applyUpload(
        unit: String,
        time: Int,
        customHelp: Int,
        help: Int,
        userId: String,
        speed: Double/*, history: String?*/
    ) {
        val avObject = AVObject("MwordsResult").apply {
            put("unit", unit)
            put("timer", time)
            put("diyhelp", customHelp)
            put("help", help)
            put("userid", userId)
            put("speed", speed)
        }
        avObject.saveInBackground().subscribe(object : Observer<AVObject> {
            override fun onNext(t: AVObject) {
                BottomDialog().create(this@MwordsActivity).apply {
                    setTitle(R.string.success)
                    setContent(R.string.upload_success)
                    setButton1(android.R.string.ok) {
                        close()
                        applyUploaded(avObject.objectId)
                    }
                    setCancelAble(false)
                    show()
                }
            }

            override fun onError(e: Throwable) {
                val error = e as AVException
                if (error.code == 142) {
                    val code = error.message?.substring(
                        error.message!!.indexOf("(") + 1,
                        error.message!!.indexOf(")")
                    ).toInt()
                    val message = error.message?.substring(
                        error.message!!.indexOf("[") + 1,
                        error.message!!.indexOf("]")
                    )
                    BottomDialog().create(this@MwordsActivity).apply {
                        setTitle(R.string.failed)
                        setContent(
                            if (code == 202) getResString(R.string.better_record) else resources.getString(
                                R.string.error,
                                message
                            )
                        )
                        setButton1(android.R.string.ok) {
                            close()
                            if (code == 202) {
                                applyDoNotUpload(unit, time, customHelp, help, userId)
                            }
                        }
                        if (code != 202) {
                            setButton2(R.string.retry) {
                                close()
                                applyUpload(unit, time, customHelp, help, userId, speed)
                            }
                        }
                        setCancelAble(false)
                        show()
                    }
                } else {
                    BottomDialog().create(this@MwordsActivity).apply {
                        setTitle(R.string.failed)
                        setContent(resources.getString(R.string.error, error))
                        setButton1(android.R.string.ok) {
                            close()
                        }
                        setButton2(R.string.retry) {
                            close()
                            applyUpload(unit, time, customHelp, help, userId, speed)
                        }
                        setCancelAble(false)
                        show()
                    }
                }
            }

            override fun onComplete() {}
            override fun onSubscribe(d: Disposable) {}
        })
    }

    private fun applyUploaded(id: String) {
        Intent(this, ResultActivity2::class.java).apply {
            putExtra("uploaded", true)
            putExtra("id", id)
            startActivity(this)
        }
    }

    private fun applyDoNotUpload(
        unit: String,
        time: Int,
        customHelp: Int,
        help: Int,
        userId: String?
    ) {
        Intent(this, ResultActivity2::class.java).apply {
            putExtra("uploaded", false)
            putExtra("unit", unit)
            putExtra("time", time)
            putExtra("help", help)
            putExtra("customHelp", customHelp)
            putExtra("user", userId)
            putExtra("createdAt", System.currentTimeMillis())
            startActivity(this)
        }
    }

    private fun upload() {
        if (wordsList.size == index + 1 && index + 1 == binding.all.text.toString()
                .toInt() && binding.words.text.toString() == wordsList.toString()
        ) {
            val currentUser = AVUser.getCurrentUser()
            if (currentUser != null) {
                applyUpload(
                    binding.spinner.selectedItem.toString(),
                    time,
                    customHelp,
                    help,
                    currentUser.objectId,
                    wordsList.size.toDouble() / time.toDouble()
                )
            } else {
                BottomDialog().create(this).apply {
                    setTitle(R.string.failed)
                    setContent(R.string.no_login)
                    setButton1(android.R.string.ok) {
                        close()
                        applyDoNotUpload(
                            binding.spinner.selectedItem.toString(),
                            time,
                            customHelp,
                            help,
                            null
                        )
                    }
                    setCancelAble(false)
                    show()
                }
            }
        }
    }

    private fun appearNumber(srcText: String, findText: String): Int {
        var count = 0
        val pattern = Pattern.compile(findText)
        val matcher = pattern.matcher(srcText)
        while (matcher.find()) {
            count++
        }
        return count
    }

    private fun parseWordsXml(xmlData: String) {
        var data = xmlData
        data = data.replace(">\n    ", ">")
        while (data.indexOf(">    ") != -1) {
            data = data.replace(">    ", ">")
        }
        if (
            appearNumber(data, "<wordbook>") == appearNumber(data, "</wordbook>")

            && appearNumber(data, "<item>") == appearNumber(data, "</item>")
            && appearNumber(data, "<word>") == appearNumber(data, "</word>")
            && appearNumber(data, "<item>") == appearNumber(data, "<word>")

            && appearNumber(data, "<trans>") == appearNumber(data, "</trans>")
            && appearNumber(data, "<word>") == appearNumber(data, "<trans>")
        ) {
            data = data.replace("<wordbook>", "")
            data = data.replace("</wordbook>", "")
            val items = data.split("</item>")
            wordsList = MutableList(items.size - 1) { Word() }
            for (i in 0 until items.size - 1) {
                val word = Word()
                word.word = items[i].substring(
                    items[i].indexOf("<word>") + "<word>".length,
                    items[i].indexOf("</word>")
                )
                word.trans = items[i].substring(
                    items[i].indexOf("<trans><![CDATA[") + "<trans><![CDATA[".length,
                    items[i].indexOf("]]></trans>")
                )
                wordsList[i] = word
            }
        } else {
            finish()
        }
    }

    private fun error(e: Exception) {
        BottomDialog().create(this).apply {
            setTitle(R.string.failed)
            setContent(resources.getString(R.string.error, e))
            setCancelAble(false)
            setButton1(android.R.string.ok) {
                close()
                finish()
            }
        }
    }

    private fun initViews() {
        /*
            root
         */
        WindowCompat.setDecorFitsSystemWindows(window, false)
        /*
            Toolbar
         */
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.appbar.setInset(INSET_TOP)
        /*
            Button
         */
        binding.button1.setOnClickListener {
            binding.editText.setText("")
            if (customHelp != 0) {
                binding.editText.hint = wordsList[index].word.substring(
                    0,
                    if (wordsList[index].word.length >= customHelp) customHelp else wordsList[index].word.length
                )
            } else {
                binding.editText.hint = wordsList[index].word
            }
            help++
            val textView = findViewById<TextView>(R.id.help)
            if (help == textView.text.toString().toInt() + 1) {
                textView.text = (textView.text.toString().toInt() + 1).toString()
            } else {
                finish()
            }
        }
        binding.button2.setOnClickListener {
            try {
                next()
            } catch (e: InterruptedException) {
                error(e)
            }
        }
        binding.button3.setOnClickListener { init(urlList[binding.spinner.selectedItemPosition]) }
        /*
            EditText
         */
        binding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                binding.editText.hint = ""
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        /*
            User
         */
        val currentUser = AVUser.getCurrentUser()
        if (currentUser == null) {
            BottomDialog().create(this).apply {
                setTitle(R.string.no_login)
                setContent(R.string.no_login_message)
                setButton1(android.R.string.ok) {
                    close()
                }
                show()
            }
        }
    }

    private fun <T> swap(list: MutableList<T>, i: Int, j: Int) {
        val temp = list[i]
        list[i] = list[j]
        list[j] = temp
    }

    private fun <T> randomSort(list: MutableList<T>) {
        val length = list.size
        for (i in length downTo 1) {
            val random = Random()
            val randomInt = random.nextInt(i)
            swap(list, randomInt, i - 1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }

     */
}