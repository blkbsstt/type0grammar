object Thue
{
	import util.Random

	val sep = " -> "

	case class Rule(lhs: String, rhs: String)
	{
		override def toString = lhs + sep + rhs 
	}

	def main(args: Array[String]) : Unit = 
	{
		val opts = options(args.toList)
		if (!(opts contains "file")) return println(usage)
		val (state, rules) = parse(opts("file"))
		if (opts contains "debug") println(rules.mkString("\n"))
		if (opts contains "batch")
			return println(batch(state, rules, opts).toList.sortBy(_.size).mkString("\n"))
		println(execute(state, rules, opts))
	}

	val usage = """thue [-b <num>] [-d] <program_file>"""

	def options(args: List[String], opts: Map[String, String] = Map.empty) : Map[String, String] = 
		args match {
			case "-b" :: i :: rest => options(rest, opts + ("batch" -> i))
			case "-d" :: rest => options(rest, opts + ("debug" -> "true"))
			case file :: Nil => opts + ("file" -> file)
			case _ => opts
		}

	def parse(filename: String) = 
	{
		val input = io.Source.fromFile(filename).getLines
		val state = input.next

		val lhs = """(\S+)"""
		val rhs = """(\S*)"""
		val InputRegEx = (lhs + sep + rhs).r
		val Whitespace = """\s+""".r

		val rules = input.flatMap {
			case InputRegEx(lhs, rhs) => Some(Rule(lhs, rhs))
			case Whitespace => None
			case line => println("Malformed production: " + line); None
		}.toList

		(state, rules)
	}

	def execute(state: String, rules: List[Rule], opts: Map[String, String]) : String =
	{
		if (opts contains "debug") println("step: " + state)
		val matches = rules.flatMap(r => r.lhs.r.findAllIn(state).matchData.map((_,r.rhs))).toArray

		if (matches isEmpty) return state

		val (matchData, rhs) = matches(Random.nextInt(matches.size))
		val newState = matchData.before + rhs + matchData.after
		execute(newState, rules, opts)
	}

	def batch(state: String, rules: List[Rule], opts: Map[String, String]) =
	{
		var strings = Set.empty[String]
		val i = opts("batch").toInt
		while(strings.size < i) strings += execute(state, rules, opts)
		strings
	}
}
