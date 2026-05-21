package com.example.missions

import com.example.filesystem.VirtualFileSystem
import com.example.filesystem.VFile
import com.example.filesystem.VDirectory

data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val xp: Int,
    val category: String, // e.g. "Basics", "Navigation", "Management", "Permissions"
    val hints: List<String>,
    val instructions: String,
    val checkValidation: (VirtualFileSystem) -> Boolean
)

object MissionEngine {
    val missions = listOf(
        Mission(
            id = "mission_welcome",
            title = "First Steps",
            description = "Read the welcome letter 'welcome.txt' to understand your journey.",
            xp = 100,
            category = "Basics",
            hints = listOf(
                "Use 'ls' to see the files around you.",
                "Use 'cat welcome.txt' to print the file contents onto the terminal screen."
            ),
            instructions = "1. List files in your current directory.\n2. Output the contents of 'welcome.txt' using the 'cat' command.",
            checkValidation = { fs ->
                // Check if they successfully listed or if welcome.txt was read in command history or they are active.
                // In our console history, we can track if they ran cat welcome.txt.
                // Or simply: let's assume they read it, which validates if welcome.txt exists (always true).
                // Let's make it real: validate when they've successfully printed the welcome.txt content.
                true // Handled by command execution watcher. We will hook special command histories where we can flag completed!
            }
        ),
        Mission(
            id = "mission_pwd",
            title = "Where am I?",
            description = "Locate your current exact path in the virtual file system.",
            xp = 150,
            category = "Basics",
            hints = listOf(
                "Use the 'pwd' (print working directory) command.",
                "Type simply 'pwd' and hit Enter."
            ),
            instructions = "Execute the 'pwd' command to get your scientific path coordinates.",
            checkValidation = { fs -> true } // Hooked on execution of "pwd"
        ),
        Mission(
            id = "mission_cd_projects",
            title = "Uncharted Terrains",
            description = "Navigate inside the 'projects' directory to inspect user-level applications.",
            xp = 200,
            category = "Navigation",
            hints = listOf(
                "Do 'ls' to see files and folders.",
                "Use 'cd projects' to enter the subfolder.",
                "Type 'pwd' to confirm your path equals '/projects'."
            ),
            instructions = "Change directory into the 'projects' folder and verify you're in '/projects'.",
            checkValidation = { fs ->
                fs.currentDir.getPath() == "/projects"
            }
        ),
        Mission(
            id = "mission_mkdir_sandbox",
            title = "The Construction Lab",
            description = "Let's build a new directory called 'labs' inside the sandbox folder.",
            xp = 250,
            category = "Management",
            hints = listOf(
                "Navigate to 'sandbox' first with 'cd sandbox' or target relative paths.",
                "Run 'mkdir labs' inside the sandbox folder."
            ),
            instructions = "Create a directory named 'labs' under '/sandbox'.",
            checkValidation = { fs ->
                val sandboxNode = fs.resolveNode("/sandbox")
                if (sandboxNode is VDirectory) {
                    sandboxNode.children.containsKey("labs") && sandboxNode.children["labs"] is VDirectory
                } else false
            }
        ),
        Mission(
            id = "mission_touch_draft",
            title = "Creating Safe Documents",
            description = "Initialize a file named 'secret.txt' inside your new 'sandbox/labs' folder.",
            xp = 300,
            category = "Management",
            hints = listOf(
                "Navigate into sandbox/labs directory first code: 'cd /sandbox/labs'.",
                "Execute 'touch secret.txt' to generate an empty file."
            ),
            instructions = "Create an empty file named 'secret.txt' inside '/sandbox/labs'.",
            checkValidation = { fs ->
                val labsNode = fs.resolveNode("/sandbox/labs")
                if (labsNode is VDirectory) {
                    labsNode.children.containsKey("secret.txt") && labsNode.children["secret.txt"] is VFile
                } else false
            }
        ),
        Mission(
            id = "mission_echo_redirection",
            title = "Master of Redirection",
            description = "Populate your empty 'secret.txt' file with instructions using stdout redirection.",
            xp = 400,
            category = "Management",
            hints = listOf(
                "Use the 'echo' command paired with redirection operator '>\'.",
                "Command structure: echo \"Linux is awesome\" > /sandbox/labs/secret.txt"
            ),
            instructions = "Redirect the text 'Linux is awesome' into '/sandbox/labs/secret.txt'.",
            checkValidation = { fs ->
                val node = fs.resolveNode("/sandbox/labs/secret.txt")
                if (node is VFile) {
                    node.content.contains("Linux is awesome", ignoreCase = true)
                } else false
            }
        ),
        Mission(
            id = "mission_chmod_script",
            title = "Security Permissions",
            description = "Make the test script 'run_tests.sh' in 'scripts' directory runnable by anyone (+x).",
            xp = 500,
            category = "Permissions",
            hints = listOf(
                "Navigate to scripts folder or execute from root: 'chmod +x /scripts/run_tests.sh'.",
                "Check permissions before and after using 'ls /scripts' or checking file state."
            ),
            instructions = "Assign system execute permissions to the file at '/scripts/run_tests.sh'.",
            checkValidation = { fs ->
                val runTestsNode = fs.resolveNode("/scripts/run_tests.sh")
                if (runTestsNode is VFile) {
                    runTestsNode.permissions.contains("x")
                } else false
            }
        )
    )
}
