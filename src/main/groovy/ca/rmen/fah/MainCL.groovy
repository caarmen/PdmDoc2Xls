package ca.rmen.fah

@Grapes([
        @Grab(group = 'net.sourceforge.jexcelapi', module = 'jxl', version = '2.6.12'),
])

/**
 * Created by calvarez on 28/02/15.
 * I don't know what I'm doing with groovy and IntelliJ.
 * When I run my groovy scripts on the command line, I <b>need</b>
 * to include the above Grab annotations.  When I run them from
 * IntelliJ, I <b>cannot</b> have the Grab annotations.
 * So, as a workaround, I run this script on the command line,
 * and the Main script from IntelliJ
 */
class MainCL {
    static main(args) {
        Main.main(args)
    }
}
