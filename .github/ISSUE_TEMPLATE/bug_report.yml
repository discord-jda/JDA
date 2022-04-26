name: Bug Report
description: Found a Bug that needs to be fixed?
body:
- type: markdown
  attributes:
    value: |-
      Please join the [Discord Server](https://discord.gg/0hMr4ce0tIl3SLv5) for questions or ask them in [our Discussions](https://github.com/DV8FromTheWorld/JDA/discussions).
      
      Keep in mind that this isn't the place to learn Java.  
      Please head over to [StackOverflow](https://stackoverflow.com/questions/tagged/java) for your general programming questions.
- type: checkboxes
  attributes:
    label: General Troubleshooting
    description: You confirm to have made the following checks first.
    options:
      - label: I have checked for similar issues on the Issue-tracker.
        required: true
      - label: I have updated to the [latest JDA version](https://ci.dv8tion.net/job/JDA/lastSuccessfulBuild/)
        required: true
      - label: I have checked the branches or the maintainers' PRs for upcoming bug fixes.
        required: true
- type: textarea
  attributes:
    label: "Expected Behaviour"
    description: "What did you expect JDA to do?"
    placeholder: "JDA should do ..."
  validations:
    required: true
- type: textarea
  attributes:
    label: "Code Example for Reproduction Steps"
    description: |-
      Please add the code you use to reproduce this problem.  
      Make sure to remove or replace any sensitive data like your Bot's token.
      Leave this empty or put "N/A" if you don't have a reproducible setup.
      
      The provided text will be rendered as Java code, so you don't have to provide a Code block for it.
    render: java
    placeholder: |-
      public void causeError() {
          throw new Exception("Error!");
      }
  validations:
    required: true
- type: textarea
  attributes:
    label: "Code for JDABuilder or DefaultShardManagerBuilder used"
    description: |-
      Please provide the code used to create your JDA or ShardManager instance.  
      Make sure to remove or replace any sensitive data like your Bot's token.
      
      The provided text will be rendered as Java code, so you don't have to provide a Code block for it.
    render: java
    placeholder: 'JDA jda = JDABuilder.createDefault("token").build();'
  validations:
    required: true
- type: textarea
  attributes:
    label: "Exception or Error"
    description: |-
      Share any Exception or Error you encountered.  
      Make sure to put it into a code block for better formatting.
      Leave this blank or put "N/A" if you don't have an Exception or Error.
      
      The provided text will be rendered as code, so you don't have to provide a Code block for it.
    render: yesyes # Unknown code-names = no highlighting.
    placeholder: "java.lang.NullPointerException: null"
