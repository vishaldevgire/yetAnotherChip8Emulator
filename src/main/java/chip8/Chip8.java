package chip8;

import sound.BeepGenerator;

import java.io.IOException;
import java.util.Random;

public class Chip8 {
    public boolean drawFlag = true;
    private static final int MEM_SIZE = 4096, PIXELS = 64 * 32;
    private static final int[] FONT_SET = {
            0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
            0x20, 0x60, 0x20, 0x20, 0x70, // 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
            0x90, 0x90, 0xF0, 0x10, 0x10, // 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
            0xF0, 0x10, 0x20, 0x40, 0x40, // 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
            0xF0, 0x90, 0xF0, 0x90, 0x90, // A
            0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
            0xF0, 0x80, 0x80, 0x80, 0xF0, // C
            0xE0, 0x90, 0x90, 0x90, 0xE0, // D
            0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
            0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    };

    private char opcode;
    private byte[] memory; //MEM_SIZE
    private char[] V;
    private char I, pc;
    private char delay_timer, sound_timer;
    private char stack[]; //16
    private char sp;

    public char key[]; //16
    public char gfx[]; //PIXELS

    private Chip8() {
        pc = 0x200;
        opcode = 0;
        I = 0;
        sp = 0;

        memory = new byte[MEM_SIZE];
        V = new char[16];
        gfx = new char[PIXELS];
        stack = new char[16];
        key = new char[16];

        delay_timer = sound_timer = 0;

        for (char i = 0; i < 80; i++) {
            memory[i] = (byte) FONT_SET[i];
        }
    }

    public static Chip8 getInstance() {
        return new Chip8();
    }

    public void loadGame(byte[] buffer) throws IOException {
        for (int i = 0; i < buffer.length; i++) {
            memory[i + 512] = buffer[i];
        }
    }

    private char reg(int index) {
        return (char) (V[index] & 0xff);
    }

    private void reg(int index, int value) {
        V[index] = (char) (0xff & value);
    }

    public void emulateCycle() {
        // Fetch opcode
        opcode = (char) ((memory[pc] & 0x00ff) << 8 | (memory[pc + 1] & 0x00ff));

        // Process opcode
        switch (opcode & 0xF000) {
            case 0x0000:
                switch (opcode & 0x000F) {
                    case 0x0000: // 0x00E0: Clears the screen
                        for (int i = 0; i < 2048; ++i)
                            gfx[i] = 0x0;
                        drawFlag = true;
                        pc += 2;
                        break;

                    case 0x000E: // 0x00EE: Returns from subroutine
                        --sp;            // 16 levels of stack, decrease stack pointer to prevent overwrite
                        pc = stack[sp];    // Put the stored return address from the stack back into the program counter
                        pc += 2;        // Don't forget to increase the program counter!
                        break;

                    default:
                        System.out.printf("Unknown opcode [0x0000]: 0x%X\n", opcode);
                }
                break;

            case 0x1000: // 0x1NNN: Jumps to address NNN
                pc = (char) (opcode & 0x0FFF);
                break;

            case 0x2000: // 0x2NNN: Calls subroutine at NNN.
                stack[sp] = pc;            // Store current address in stack
                ++sp;                    // Increment stack pointer
                pc = (char) (opcode & 0x0FFF);    // Set the program counter to the address at NNN
                break;

            case 0x3000: // 0x3XNN: Skips the next instruction if VX equals NN
                pc += reg((opcode & 0x0F00) >> 8) == (opcode & 0x00FF) ? 4 : 2;
                break;

            case 0x4000: // 0x4XNN: Skips the next instruction if VX doesn't equal NN
                pc += reg((opcode & 0x0F00) >> 8) != (opcode & 0x00FF) ? 4 : 2;
                break;

            case 0x5000: // 0x5XY0: Skips the next instruction if VX equals VY.
                pc += (reg((opcode & 0x0F00) >> 8) == (reg(opcode & 0x00F0) >> 4)) ? 4 : 2;
                break;

            case 0x6000: // 0x6XNN: Sets VX to NN.
                reg((opcode & 0x0F00) >> 8, opcode & 0x00FF);
                pc += 2;
                break;

            case 0x7000: // 0x7XNN: Adds NN to VX.
                reg((opcode & 0x0F00) >> 8, reg((opcode & 0x0F00) >> 8) + (opcode & 0x00FF));
                pc += 2;
                break;

            case 0x8000:
                switch (opcode & 0x000F) {
                    case 0x0000: // 0x8XY0: Sets VX to the value of VY
                        reg((opcode & 0x0F00) >> 8, reg((opcode & 0x00F0) >> 4));
                        pc += 2;
                        break;

                    case 0x0001: // 0x8XY1: Sets VX to "VX OR VY"
                        reg((opcode & 0x0F00) >> 8, reg((opcode & 0x0F00) >> 8) | reg((opcode & 0x00F0) >> 4));
                        pc += 2;
                        break;

                    case 0x0002: // 0x8XY2: Sets VX to "VX AND VY"
                        reg((opcode & 0x0F00) >> 8, reg((opcode & 0x0F00) >> 8) & reg((opcode & 0x00F0) >> 4));
                        pc += 2;
                        break;

                    case 0x0003: // 0x8XY3: Sets VX to "VX XOR VY"
                        reg((opcode & 0x0F00) >> 8, reg((opcode & 0x0F00) >> 8)  ^ reg((opcode & 0x00F0) >> 4));
                        pc += 2;
                        break;

                    case 0x0004: // 0x8XY4: Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't
                        if (reg((opcode & 0x00F0) >> 4) > (0xFF - reg((opcode & 0x0F00) >> 8))) {
                            reg(0xF, 1); //carry
                        } else {
                            reg(0xF, 0);
                        }
                        reg((opcode & 0x0F00) >> 8, reg((opcode & 0x0F00) >> 8)  + reg((opcode & 0x00F0) >> 4));
                        pc += 2;
                        break;

                    case 0x0005: // 0x8XY5: VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't
                        if (reg((opcode & 0x00F0) >> 4) > reg((opcode & 0x0F00) >> 8)) {
                            reg(0xF, 0); // there is a borrow
                        } else {
                            reg(0xF, 1);
                        }
                        reg((opcode & 0x0F00) >> 8, (opcode & 0x0F00) >> 8 - (opcode & 0x00F0) >> 4);
                        pc += 2;
                        break;

                    case 0x0006: // 0x8XY6: Shifts VX right by one. VF is set to the value of the least significant bit of VX before the shift
                        reg(0xF, reg((opcode & 0x0F00) >> 8) & 0x1);
                        reg((opcode & 0x0F00) >> 8, reg((opcode & 0x0F00) >> 8) >> 1);
                        pc += 2;
                        break;

                    case 0x0007: // 0x8XY7: Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't
                        if (reg((opcode & 0x0F00) >> 8) > reg((opcode & 0x00F0) >> 4)) {    // VY-VX
                            reg(0xF, 0); // there is a borrow
                        } else {
                            reg(0xF, 1);
                        }
                        reg((opcode & 0x0F00) >> 8, reg((opcode & 0x00F0) >> 4) - reg((opcode & 0x0F00) >> 8));
                        pc += 2;
                        break;

                    case 0x000E: // 0x8XYE: Shifts VX left by one. VF is set to the value of the most significant bit of VX before the shift
                        reg(0xF, reg((opcode & 0x0F00) >> 8) >> 7);
                        reg((opcode & 0x0F00) >> 8, reg((opcode & 0x0F00) >> 8) << 1);
                        pc += 2;
                        break;

                    default:
                        System.out.printf("Unknown opcode [0x8000]: 0x%X\n", opcode);
                }
                break;

            case 0x9000: // 0x9XY0: Skips the next instruction if VX doesn't equal VY
                pc += (reg((opcode & 0x0F00) >> 8) != reg((opcode & 0x00F0) >> 4)) ? 4 : 2;
                break;

            case 0xA000: // ANNN: Sets I to the address NNN
                I = (char) (opcode & 0x0FFF);
                pc += 2;
                break;

            case 0xB000: // BNNN: Jumps to the address NNN plus V0
                pc = (char) ((opcode & 0x0FFF) + reg(0));
                break;

            case 0xC000: // CXNN: Sets VX to a random number and NN
                reg((opcode & 0x0F00) >> 8, (rand() % 0xFF) & (opcode & 0x00FF));
                pc += 2;
                break;

            case 0xD000: // DXYN: Draws a sprite at coordinate (VX, VY) that has a width of 8 pixels and a height of N pixels.
                // Each row of 8 pixels is read as bit-coded starting from memory location I;
                // I value doesn't change after the execution of this instruction.
                // VF is set to 1 if any screen pixels are flipped from set to unset when the sprite is drawn,
                // and to 0 if that doesn't happen
            {
                int x = reg((opcode & 0x0F00) >> 8);
                int y = reg((opcode & 0x00F0) >> 4);
                int height = opcode & 0x000F;
                int pixel;

                reg(0xF, 0);
                for (int yline = 0; yline < height; yline++) {
                    pixel = memory[I + yline];
                    for (int xline = 0; xline < 8; xline++) {
                        if ((pixel & (0x80 >> xline)) != 0) {
                            int index = (x + xline + ((y + yline) * 64));
                            //if (index < 2048) {
                                if (gfx[index] == 1) {
                                    reg(0xF, 1);
                                }
                                gfx[index] ^= 1;
                            //}
                        }
                    }
                }

                drawFlag = true;
                pc += 2;
            }
            break;

            case 0xE000:
                switch (opcode & 0x00FF) {
                    case 0x009E: // EX9E: Skips the next instruction if the key stored in VX is pressed
                        pc += (key[reg((opcode & 0x0F00) >> 8)] != 0) ? 4 : 2;
                        break;

                    case 0x00A1: // EXA1: Skips the next instruction if the key stored in VX isn't pressed
                        pc += (key[reg((opcode & 0x0F00) >> 8)] == 0) ? 4 : 2;
                        break;

                    default:
                        System.out.printf("Unknown opcode [0xE000]: 0x%X\n", opcode);
                }
                break;

            case 0xF000:
                switch (opcode & 0x00FF) {
                    case 0x0007: // FX07: Sets VX to the value of the delay timer
                        reg((opcode & 0x0F00) >> 8, delay_timer & 0x00ff);
                        pc += 2;
                        break;

                    case 0x000A: // FX0A: A key press is awaited, and then stored in VX
                    {
                        boolean keyPress = false;

                        for (int i = 0; i < 16; ++i) {
                            if (key[i] != 0) {
                                reg((opcode & 0x0F00) >> 8, i);
                                keyPress = true;
                            }
                        }

                        // If we didn't received a keypress, skip this cycle and try again.
                        if (!keyPress)
                            return;

                        pc += 2;
                    }
                    break;

                    case 0x0015: // FX15: Sets the delay timer to VX
                        delay_timer = reg((opcode & 0x0F00) >> 8);
                        pc += 2;
                        break;

                    case 0x0018: // FX18: Sets the sound timer to VX
                        sound_timer = reg((opcode & 0x0F00) >> 8);
                        pc += 2;
                        break;

                    case 0x001E: // FX1E: Adds VX to I
                        if (I + reg((opcode & 0x0F00) >> 8) > 0xFFF)    // VF is set to 1 when range overflow (I+VX>0xFFF), and 0 when there isn't.
                            reg(0xF, 1);
                        else
                            reg(0xF, 0);
                        I += reg((opcode & 0x0F00) >> 8);
                        pc += 2;
                        break;

                    case 0x0029: // FX29: Sets I to the location of the sprite for the character in VX. Characters 0-F (in hexadecimal) are represented by a 4x5 font
                        I = (char) (reg((opcode & 0x0F00) >> 8) * 0x5);
                        pc += 2;
                        break;

                    case 0x0033: // FX33: Stores the Binary-coded decimal representation of VX at the addresses I, I plus 1, and I plus 2
                        memory[I] = (byte) (reg((opcode & 0x0F00) >> 8) / 100);
                        memory[I + 1] = (byte) ((reg((opcode & 0x0F00) >> 8) / 10) % 10);
                        memory[I + 2] = (byte) ((reg((opcode & 0x0F00) >> 8) % 100) % 10);
                        pc += 2;
                        break;

                    case 0x0055: // FX55: Stores V0 to VX in memory starting at address I
                        for (int i = 0; i <= ((opcode & 0x0F00) >> 8); ++i) {
                            memory[I + i] = (byte) reg(i);
                        }

                        // On the original interpreter, when the operation is done, I = I + X + 1.
                        I += ((opcode & 0x0F00) >> 8) + 1;
                        pc += 2;
                        break;

                    case 0x0065: // FX65: Fills V0 to VX with values from memory starting at address I
                        for (int i = 0; i <= ((opcode & 0x0F00) >> 8); ++i) {
                            reg(i, memory[I + i]);
                        }
                        // On the original interpreter, when the operation is done, I = I + X + 1.
                        I += ((opcode & 0x0F00) >> 8) + 1;
                        pc += 2;
                        break;

                    default:
                        System.out.printf("Unknown opcode [0xF000]: 0x%X\n", opcode);
                }
                break;

            default:
                System.out.printf("Unknown opcode: 0x%X\n", opcode);
        }

        // Update timers
        if (delay_timer > 0)
            --delay_timer;

        if (sound_timer > 0) {
            if (sound_timer == 1) {
                BeepGenerator.beep();
            }
            --sound_timer;
        }
    }

    private int rand() {
        return new Random().nextInt(255);
    }
}
