# Prerequisite: brew install --cask gpg-suite

echo Enter name of encrypted file including its extension
read -r input
if [[ -z $input ]]; then
    echo "Input should not be empty"; exit 1
fi

echo Enter name for output file
read -r output
if [[ -z $input ]]; then
    echo "Output should not be empty"; exit 1
fi

echo Enter key for decryption
read -r key
if [[ -z $key ]]; then
    echo "Key should not be empty"; exit 1
fi

echo Starting decryption...

gpg --quiet --batch --yes --decrypt --passphrase="${key}" --output ${output} ${input}

echo Your decrypted file "${output}" is created.