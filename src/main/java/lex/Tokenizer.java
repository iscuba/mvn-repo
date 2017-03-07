package lex;

import com.sun.javafx.fxml.expression.Expression;
import errors.LexicalError;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.CharBuffer;

/*
 * TODO: Assignment #1
 */
public class Tokenizer
{
	private CharStream stream = null;
	// create a buffer to hold read chars
	private CharBuffer buff;
	// create Token for last token returned
	private Token lastToken = null;
	//Length of the longest possible identifier
	private int MAX_ID_LENGTH;
	private Token t = new Token();

	public Tokenizer(String filename) throws IOException, LexicalError
	{
		super();
		init(new CharStream(filename));
	}

	/** Used during testing to read files from the classpath. */
	public Tokenizer(URL url) throws IOException, LexicalError
	{
		super();
		init(new CharStream(url));
	}

	public Tokenizer(File file) throws IOException, LexicalError
	{
		super();
		init(new CharStream(file));
	}

	protected void init(CharStream stream)
	{
		this.stream = stream;
		MAX_ID_LENGTH = 64;
//		keywordTable = new KeywordTable();
		setLastToken(new Token());
		// TODO more initialization will be needed...
	}

	public int getLineNumber()
	{
		return stream.lineNumber();
	}

	public Token getLastToken(){
		return lastToken;
	}

	public void setLastToken(Token t){
		this.lastToken = t;
	}
	//___________________
	//     getNextToken
	//____________________
	// Returns the next token

	public Token getNextToken() throws LexicalError
	{
		t = new Token();

		lastToken = new Token();
		lastToken.setType(t.getType());
		lastToken.setValue(t.getValue());

		while (true) {
			char seen = stream.currentChar();
			//if we see a newline op or blank space, go to the next char
			if ( seen == '\n' || seen == ' '){
				seen = stream.currentChar();

			}
			if (Character.isDigit(seen)) {
				t = readDigit(seen);

			} else if (Character.isLetter(seen)){
				t = readLetter(seen);

			} else if (isMulOp(seen)){
				t = readMulOp(seen);

			} else if (isAddOp(seen)) {
				t = readAddOp(seen);

			} else if (isRelOpOrAssign(seen)){
				t = readRelOp(seen);

			} else {
				readOther(seen);
			}
			return t;
		}

	}

	private Token readDigit(Character seen) throws LexicalError{


	}

	//------------------------
    //   Makes a StringBuffer of all the letters found next to each other, and calls a sister
    //   Function which identifies the proper Token Type for the string found and sets the
    //   Token type accordingly;
    //-------------------------

	private Token readLetter(Character seen) throws LexicalError{
        StringBuffer name = new StringBuffer().append(seen);
        seen = stream.currentChar();
        while (Character.isLetter(seen)) {
            if (MAX_ID_LENGTH <= name.length()) {
                throw LexicalError.IdentifierTooLong(name.toString());
            } else {
                name.append(seen);
                seen = stream.currentChar();
            }
        }
        stream.pushBack(seen);
        return whichTokenType(name.toString());
	}

    //whichTokenType(String str) ---> Token
	//--------------------------------------
    //    Takes a string and determines whether it is an identifier or
    //    keyword, and sets the token type accordingly.
	private Token whichTokenType(String str) {

    }


	private Token readMulOp(Character seen) throws LexicalError {
        switch (seen) {
            case '*':
                t.setType(TokenType.MULOP);
                t.setOpType(Token.OperatorType.MULTIPLY);
                break;
            case '/' :
                t.setType(TokenType.MULOP);
                t.setOpType(Token.OperatorType.DIVIDE);
                break;

        }
	}

	private Token readAddOp(Character seen) throws LexicalError{
        TokenType lastTokenType = getLastToken().getType();
        if (lastTokenType == TokenType.RIGHTPAREN ||
                lastTokenType == TokenType.RIGHTBRACKET ||
                lastTokenType == TokenType.IDENTIFIER ||
                lastTokenType == TokenType.INTCONSTANT ||
                lastTokenType == TokenType.REALCONSTANT) {
            if (seen == '+') {
                t.setType(TokenType.ADDOP);
                t.setOpType(Token.OperatorType.ADD);
            } else if (seen == '-') {
                t.setType(TokenType.ADDOP);
                t.setOpType(Token.OperatorType.SUBTRACT);
            } else {
                //Throw error
                throw LexicalError.IllegalCharacter(seen, getLineNumber());

            }
        } else {

            if (seen == '-') {
                t.setType(TokenType.UNARYMINUS);
            }
            else if (seen == '+') {
                t.setType(TokenType.UNARYPLUS);

            } else {
                //Do I throw an error??

            }
        }
        return t;
	}

	private Token readRelOp(Character seen) throws LexicalError{
	    char nextChar;
	    switch (seen) {
            case '=':
                t.setOpType(Token.OperatorType.EQUAL);
                t.setType(TokenType.RELOP);
                break;
            case '>':
                nextChar = stream.currentChar();
                if (nextChar == '=') {
                    t.setOpType(Token.OperatorType.GREATERTHANOREQUAL);
                    t.setType(TokenType.RELOP);
                } else {
                    t.setOpType(Token.OperatorType.GREATERTHAN);
                    t.setType(TokenType.RELOP);
                    stream.pushBack(lookahead);
                }
                break;

            case '<':
                nextChar = stream.currentChar();
                if (nextChar == '=') {
                    t.setOpType(Token.OperatorType.LESSTHANOREQUAL);
                    t.setType(TokenType.RELOP);
                } else if (nextChar == '>') {
                    t.setOpType(Token.OperatorType.NOTEQUAL);
                    t.setType(TokenType.RELOP);
                } else {
                    t.setOpType(Token.OperatorType.LESSTHAN);
                    t.setType(TokenType.RELOP);
                    stream.pushBack(nextChar);
                }
                break;


        }
        return t;
	}

	private Token readOther(Character seen) {
        switch (seen) {
            case '(':
                t.setType(TokenType.LEFTPAREN);
                break;
            case ')':
                t.setType(TokenType.RIGHTPAREN);
                break;
            case '[':
                t.setType(TokenType.LEFTBRACKET);
                break;
            case ']':
                t.setType(TokenType.RIGHTBRACKET);
                break;
            case ';':
                t.setType(TokenType.SEMICOLON);
                break;
            case ':':
                char s = stream.currentChar();
                if (s == '=') {
                    t.setType(TokenType.ASSIGNOP);
                } else {
                    t.setType(TokenType.COLON);
                    stream.pushBack(s);
                }
                break;
            case ',':
                t.setType(TokenType.COMMA);
                break;
            case '.':
                char lookahead = stream.currentChar();
                // if the next thing is also a dot, then we got doubledot
                if (lookahead == '.') {
                    t.setType(TokenType.DOUBLEDOT);
                } else {
                    // if the next thing isn't a dot, then we have an end marker
                    stream.pushBack(lookahead);
                    t.setType(TokenType.ENDMARKER);
                }
                break;
            case CharStream.EOF:
                t.setType(TokenType.ENDOFFILE);
                break;
            default:
                // If it isn't valid... throw an error
                if (!stream.valid(ch)) {
                    throw LexicalError.IllegalCharacter(ch, stream.lineNumber());
                }
        }
        return t;


	}

	//------------------------
	//     Identifiers
	//------------------------

	private Boolean isMulOp(Character seen) {
        return seen == '*' || seen == '/';
	}

	private Boolean isAddOp(Character seen) {
        return seen == '+' || seen == '-';
	}

	// Returns T if we saw the beginning of a Relational Operator
	private Boolean isRelOpOrAssign(Character seen) {
		return seen == '=' || seen == '<' || seen == '>';
	}

	// TODO Much (much) more code goes here...
}
