# Adding Language

## Generating Translations

Refer the following Google Sheet (might be out of date) to get a list of all strings which require translations.
https://docs.google.com/spreadsheets/d/1NAl8xWGDRj6MmLHKyESONAAyudOHdFnSbaqF1Pr28sE/edit?usp=sharing

Use the Sheet and create a xml file for a particular column and corresponding keys.

## Adding Support for New Language

1. In `strings.xml (en)`, add entries for the new language. For example, for Hindi, add the following lines
```xml
<string>

    <string name="language_hi">Hindi</string>
    <string name="language_hi_key" translatable="false">hi-rIN</string>
</string>
```
2. Add `language_hi` key in every strings file.
3. Add the new language to the strings array in `strings.xml (en)`
```xml
<string>
    <string-array name="preference_entries_language" translatable="false">
        <item>@string/language_hi</item>
    </string-array>

    <string-array name="preference_entries_keys_language" translatable="false">
        <item>@string/language_hi_key</item>
    </string-array>
</string>
```
This step has to be performed in the English file only.
4. Create a PR with a reference to the Issue [#548](https://github.com/amahi/android/issues/548)
