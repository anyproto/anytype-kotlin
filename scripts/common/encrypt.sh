# Prerequisite: brew install --cask gpg-suite

echo Enter input filename
read -r input
if [[ -z $input ]]; then
    echo "Input should not be empty"; exit 1
fi

echo Enter output filenmae
read -r output
if [[ -z $output ]]; then
    echo "Output should not be empty"; exit 1
fi

echo Enter key for encryption
read -r key
if [[ -z $key ]]; then
    echo "Key should not be empty"; exit 1
fi

echo Starting encryption...

gpg --batch --yes --passphrase="${key}" --cipher-algo AES256 --symmetric --output ${output}.gpg ${input}

echo Your encrypted file "${output}.gpg" is created.