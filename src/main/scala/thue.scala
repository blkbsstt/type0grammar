object Thue {
	val defaultSep = " -> "

	case class Rule(lhs: String, sep: String, rhs: String) {
		override def toString = lhs + sep + rhs
	}

	def main(args: Array[String]): Unit = {
		var opts = options(args.toList)
		opts = opts.updated("sep", opts.get("sep").getOrElse(defaultSep))

		if (!(opts contains "file")) return println(usage)

		val (state, rules) = parse(opts("file"), opts("sep"))
		if (opts contains "debug") println(rules.mkString("\n"))
		if (!((opts contains "batch") || (opts contains "times")))
			opts += ("batch" -> "1")

		val strings = batch(state, rules, opts)
		if (!(opts contains "unsorted")) println(strings.toList.sorted.mkString("\n"))
	}

	val usage = """scala thue.scala [-b num] [-t num] [-s sep] [-d] [-u] program_file"""

	def options(args: List[String], opts: Map[String, String] = Map.empty): Map[String, String] =
		args match {
			case list :: rest if (list startsWith "-") && !(list startsWith "--") && (list.tail.length > 1) ⇒
				options(list.tail.map("-" + _) ++: rest, opts)
			case "-b" :: i :: rest ⇒ options(rest, opts + ("batch" -> i))
			case "-t" :: i :: rest ⇒ options(rest, opts + ("times" -> i))
			case "-s" :: sep :: rest ⇒ options(rest, opts + ("sep" -> sep))
			case "-d" :: rest ⇒ options(rest, opts + ("debug" -> "true"))
			case "-u" :: rest ⇒ options(rest, opts + ("unsorted" -> "true"))
			case "--batch" :: rest ⇒ options("-b" :: rest, opts)
			case "--tries" :: rest ⇒ options("-t" :: rest, opts)
			case "--times" :: rest ⇒ options("-t" :: rest, opts)
			case "--separator" :: rest ⇒ options("-s" :: rest, opts)
			case "--sep" :: rest ⇒ options("-s" :: rest, opts)
			case "--debug" :: rest ⇒ options("-d" :: rest, opts)
			case "--unsorted" :: rest ⇒ options("-u" :: rest, opts)
			case file :: Nil ⇒ opts + ("file" -> file)
			case badParam :: rest ⇒ println("Unsupported parameter " + badParam); options(rest, opts)
			case _ ⇒ opts
		}

	def parse(filename: String, sep: String) = {
		val input = io.Source.fromFile(filename).getLines
		val state = input.next

		val lhs = """(.+?)"""
		val rhs = """(.*?)"""
		val InputRegEx = (lhs + sep + rhs).r
		val Whitespace = """(\s*)""".r

		val rules = input.flatMap {
			case InputRegEx(lhs, rhs) ⇒ Some(Rule(lhs, sep, rhs))
			case Whitespace(_) ⇒ None
			case line ⇒ println("Malformed production: " + line); None
		} toList

		(state, rules)
	}

	def execute(state: String, rules: List[Rule], opts: Map[String, String]): String = {
		import util.Random
		if (opts contains "debug") println("> " + state)
		val matches = rules.flatMap(r ⇒ r.lhs.r.findAllIn(state).matchData.map((_, r.rhs))).toArray

		if (matches isEmpty) return state

		val (matchData, rhs) = matches(Random.nextInt(matches.size))
		val newState = matchData.before + rhs + matchData.after
		execute(newState, rules, opts)
	}

	def batch(state: String, rules: List[Rule], opts: Map[String, String]) = {
		def loop(strings: Set[String], t: Int): Set[String] =
			if (((opts contains "batch") && (strings.size >= opts("batch").toInt))
				|| ((opts contains "times") && (t >= opts("times").toInt))) strings
			else {
				if (opts contains "debug") println("Starting derivation")
				val s = execute(state, rules, opts)
				if ((opts contains "unsorted") && !strings(s)) println(s)
				loop(strings + s, t + 1)
			}
		loop(Set.empty[String], 0)
	}
}
