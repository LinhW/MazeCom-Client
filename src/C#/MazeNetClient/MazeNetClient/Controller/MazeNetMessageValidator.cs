using System;
using System.IO;
using System.Xml;
using System.Xml.Schema;

namespace MazeNetClient
{
	public static class MazeNetMessageValidator
	{
		public static string xsd = new StreamReader(Path.Combine(Path.GetDirectoryName(System.Reflection.Assembly.GetExecutingAssembly().Location), "Model\\XSD\\mazeCom.xsd")).ReadToEnd();
		public static string lastError = null;

		public static bool validate(string xml)
		{
			if (xsd == null) {
				return false;
			}

			XmlReaderSettings settings = new XmlReaderSettings();
			settings.ValidationType = ValidationType.Schema;

			XmlSchema schema = new XmlSchema();
			schema = XmlSchema.Read (new StringReader (xsd), schemaValidation);

			settings.Schemas.Add (schema);
			settings.ValidationFlags |= XmlSchemaValidationFlags.ProcessInlineSchema;
			settings.ValidationFlags |= XmlSchemaValidationFlags.ReportValidationWarnings;
			settings.ValidationEventHandler += new ValidationEventHandler(MazeNetMessageValidator.validation);

			XmlReader reader = XmlReader.Create(xml, settings);
			while (reader.Read());

			return lastError == null;
		}

		static void validation (object sender, ValidationEventArgs e) 
		{
			if (e.Severity==XmlSeverityType.Warning)
				Console.WriteLine("\tWarning: Matching schema not found.  No validation occurred." + e.Message);
			else
			{
				Console.WriteLine("\tValidation error: " + e.Message);
				MazeNetMessageValidator.lastError = e.Message;
			}
		}

		static void schemaValidation (object sender, ValidationEventArgs e)
		{
			if (e.Severity==XmlSeverityType.Warning)
				Console.WriteLine("\tWarning: Matching schema not found.  No validation occurred." + e.Message);
			else
			{
				Console.WriteLine("\tValidation error: " + e.Message);
				MazeNetMessageValidator.lastError = e.Message;
			}
		}
	}
}

