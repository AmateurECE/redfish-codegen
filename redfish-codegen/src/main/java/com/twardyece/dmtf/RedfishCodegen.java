package com.twardyece.dmtf;

import org.apache.commons.cli.*;

/**
 * Redfish Code Generator for the Rust language
 * Based on swagger-parser v3
 */
public class RedfishCodegen
{
    public static void main( String[] args )
    {
        Option apiDirectoryOption = new Option("apiDirectory", true, "Directory containing openapi resource files");
        apiDirectoryOption.setRequired(true);
        Option crateDirectoryOption = new Option("crateDirectory", true, "Directory containing Cargo.toml, into which output sources are written");
        crateDirectoryOption.setRequired(true);

        Options options = new Options();
        options.addOption(apiDirectoryOption);
        options.addOption(crateDirectoryOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine command = parser.parse(options, args);

            String apiDirectory = command.getOptionValue("apiDirectory");
            String crateDirectory = command.getOptionValue("crateDirectory");

            System.out.println(apiDirectory);
            System.out.println(crateDirectory);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("RedfishCodegen", options);
            System.exit(1);
        }
    }
}
