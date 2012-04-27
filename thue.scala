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
		val optKeys = opts.keySet
		if (!(optKeys contains "file")) return println(usage)
		val (state, rules) = parse(opts("file"))
		if (optKeys contains "batch")
			return println(batch(state, rules, opts("batch").toInt).toList.sortBy(_.size).mkString("\n"))
		println(execute(state, rules))
	}

	val usage = """thue [-b <num>] <program_file>"""

	def options(args: List[String], opts: Map[String, String] = Map.empty[String,String]) : Map[String, String] = 
		args match {
			case "-b" :: i :: rest => options(rest, opts + ("batch" -> i))
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

		(state, rules.reverse)
	}

	def execute(state: String, rules: List[Rule]) : String =
	{
		val matches = rules.flatMap(r => r.lhs.r.findAllIn(state).matchData.map((_,r.rhs))).toArray

		if (matches isEmpty) return state

		val (matchData, rhs) = matches(Random.nextInt(matches.size))
		val newState = matchData.before + rhs + matchData.after
		println("step: " + newState)
		execute(newState, rules)
	}

	def batch(state: String, rules: List[Rule], i: Int) =
	{
		var strings = Set.empty[String]
		while(strings.size < i) strings += execute(state, rules)
		strings
	}
}
